package com.ksyun.ks3.services;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.Header;

import android.content.Context;
import android.util.Log;

import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.model.Bucket;
import com.ksyun.ks3.model.ObjectListing;
import com.ksyun.ks3.model.ObjectMetadata;
import com.ksyun.ks3.model.PartETag;
import com.ksyun.ks3.model.acl.AccessControlList;
import com.ksyun.ks3.model.acl.AccessControlPolicy;
import com.ksyun.ks3.model.acl.Authorization;
import com.ksyun.ks3.model.acl.CannedAccessControlList;
import com.ksyun.ks3.model.result.CompleteMultipartUploadResult;
import com.ksyun.ks3.model.result.CopyResult;
import com.ksyun.ks3.model.result.GetObjectResult;
import com.ksyun.ks3.model.result.HeadObjectResult;
import com.ksyun.ks3.model.result.InitiateMultipartUploadResult;
import com.ksyun.ks3.model.result.ListPartsResult;
import com.ksyun.ks3.services.handler.AbortMultipartUploadResponseHandler;
import com.ksyun.ks3.services.handler.CompleteMultipartUploadResponseHandler;
import com.ksyun.ks3.services.handler.CopyObjectResponseHandler;
import com.ksyun.ks3.services.handler.CreateBucketResponceHandler;
import com.ksyun.ks3.services.handler.DeleteBucketResponceHandler;
import com.ksyun.ks3.services.handler.DeleteObjectRequestHandler;
import com.ksyun.ks3.services.handler.GetBucketACLResponceHandler;
import com.ksyun.ks3.services.handler.GetObjectACLResponseHandler;
import com.ksyun.ks3.services.handler.GetObjectResponceHandler;
import com.ksyun.ks3.services.handler.HeadBucketResponseHandler;
import com.ksyun.ks3.services.handler.HeadObjectResponseHandler;
import com.ksyun.ks3.services.handler.InitiateMultipartUploadResponceHandler;
import com.ksyun.ks3.services.handler.ListBucketsResponceHandler;
import com.ksyun.ks3.services.handler.ListObjectsResponseHandler;
import com.ksyun.ks3.services.handler.ListPartsResponseHandler;
import com.ksyun.ks3.services.handler.PutBucketACLResponseHandler;
import com.ksyun.ks3.services.handler.PutObjectACLResponseHandler;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.ks3.services.handler.UploadPartResponceHandler;
import com.ksyun.ks3.services.request.AbortMultipartUploadRequest;
import com.ksyun.ks3.services.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.services.request.CopyObjectRequest;
import com.ksyun.ks3.services.request.CreateBucketRequest;
import com.ksyun.ks3.services.request.DeleteBucketRequest;
import com.ksyun.ks3.services.request.DeleteObjectRequest;
import com.ksyun.ks3.services.request.GetBucketACLRequest;
import com.ksyun.ks3.services.request.GetObjectACLRequest;
import com.ksyun.ks3.services.request.GetObjectRequest;
import com.ksyun.ks3.services.request.HeadBucketRequest;
import com.ksyun.ks3.services.request.HeadObjectRequest;
import com.ksyun.ks3.services.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.services.request.Ks3HttpRequest;
import com.ksyun.ks3.services.request.ListBucketsRequest;
import com.ksyun.ks3.services.request.ListObjectsRequest;
import com.ksyun.ks3.services.request.ListPartsRequest;
import com.ksyun.ks3.services.request.PutBucketACLRequest;
import com.ksyun.ks3.services.request.PutObjectACLRequest;
import com.ksyun.ks3.services.request.PutObjectRequest;
import com.ksyun.ks3.services.request.UploadPartRequest;
import com.ksyun.ks3.util.Constants;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class Ks3Client implements Ks3 {
	private Ks3ClientConfiguration clientConfiguration;
	private String endpoint;
	private Authorization auth;
	private Ks3HttpExector client = new Ks3HttpExector();
	private Context context = null;
	public AuthListener authListener = null;
	private Boolean isUseAsyncMode = true;

	private Ks3Client() {
	}

	public Ks3Client(String accesskeyid, String accesskeysecret, Context context) {
		this(accesskeyid, accesskeysecret, Ks3ClientConfiguration
				.getDefaultConfiguration(), context);
	}

	public Ks3Client(String accesskeyid, String accesskeysecret,
			Ks3ClientConfiguration clientConfiguration, Context context) {
		this.auth = new Authorization(accesskeyid, accesskeysecret);
		this.clientConfiguration = clientConfiguration;
		this.context = context;
		init();
	}

	public Ks3Client(Authorization auth, Context context) {
		this(auth, Ks3ClientConfiguration.getDefaultConfiguration(), context);
	}

	public Ks3Client(Authorization auth,
			Ks3ClientConfiguration clientConfiguration, Context context) {
		this.auth = auth;
		this.clientConfiguration = clientConfiguration;
		this.context = context;
		init();
	}

	public Ks3Client(AuthListener listener, Context context) {
		this(listener, Ks3ClientConfiguration.getDefaultConfiguration(),
				context);
	}

	public Ks3Client(AuthListener listener,
			Ks3ClientConfiguration clientConfiguration, Context context) {
		this.authListener = listener;
		this.clientConfiguration = clientConfiguration;
		this.context = context;
		init();
	}

	private void init() {
		setEndpoint(Constants.ClientConfig_END_POINT);
	}

	public void setConfiguration(Ks3ClientConfiguration clientConfiguration) {
		this.clientConfiguration = clientConfiguration;
	}

	public void setEndpoint(String endpoint) throws IllegalArgumentException {
		this.endpoint = endpoint;
	}

	public AuthListener getAuthListener() {
		return authListener;
	}

	public void setAuthListener(AuthListener authListener) {
		this.authListener = authListener;
	}

	/* Service */
	@Override
	public void listBuckets(ListBucketsResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		listBuckets(new ListBucketsRequest(), resultHandler);
	}

	@Override
	public void listBuckets(ListBucketsRequest request,
			ListBucketsResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		invoke(auth, request, resultHandler);
	}

	/* Bucket ACL */
	@Override
	public void getBucketACL(String bucketName,
			GetBucketACLResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.getBucketACL(new GetBucketACLRequest(bucketName), resultHandler);
	}

	@Override
	public void getBucketACL(GetBucketACLRequest request,
			GetBucketACLResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public void putBucketACL(String bucketName,
			AccessControlList accessControlList,
			PutBucketACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.putBucketACL(
				new PutBucketACLRequest(bucketName, accessControlList),
				resultHandler);
	}

	@Override
	public void putBucketACL(String bucketName,
			CannedAccessControlList CannedAcl,
			PutBucketACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.putBucketACL(new PutBucketACLRequest(bucketName, CannedAcl),
				resultHandler);
	}

	@Override
	public void putBucketACL(PutBucketACLRequest request,
			PutBucketACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	/* Object ACL */
	@Override
	public void putObjectACL(String bucketName, String objectName,
			CannedAccessControlList accessControlList,
			PutObjectACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.putObjectACL(new PutObjectACLRequest(bucketName, objectName,
				accessControlList), resultHandler);
	}

	@Override
	public void putObjectACL(String bucketName, String objectName,
			AccessControlList accessControlList,
			PutObjectACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.putObjectACL(new PutObjectACLRequest(bucketName, objectName,
				accessControlList), resultHandler);
	}

	@Override
	public void putObjectACL(PutObjectACLRequest request,
			PutObjectACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public void getObjectACL(String bucketName, String ObjectName,
			GetObjectACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.getObjectACL(new GetObjectACLRequest(bucketName, ObjectName),
				resultHandler);
	}

	@Override
	public void getObjectACL(GetObjectACLRequest request,
			GetObjectACLResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	/* Bucket */
	@Override
	public void headBucket(String bucketname,
			HeadBucketResponseHandler resultHandler) throws Ks3ClientException,
			Ks3ServiceException {
		this.headBucket(new HeadBucketRequest(bucketname), resultHandler);
	}

	@Override
	public void headBucket(HeadBucketRequest request,
			HeadBucketResponseHandler resultHandler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public boolean bucketExists(String bucketname) throws Ks3ClientException {
		return false;
	}

	@Override
	public void createBucket(String bucketname,
			CreateBucketResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.createBucket(new CreateBucketRequest(bucketname), resultHandler);
	}

	@Override
	public void createBucket(String bucketname, AccessControlList list,
			CreateBucketResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.createBucket(new CreateBucketRequest(bucketname, list),
				resultHandler);
	}

	@Override
	public void createBucket(String bucketname, CannedAccessControlList list,
			CreateBucketResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.createBucket(new CreateBucketRequest(bucketname, list),
				resultHandler);
	}

	@Override
	public void createBucket(CreateBucketRequest request,
			CreateBucketResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		invoke(auth, request, resultHandler);
	}

	@Override
	public void deleteBucket(String bucketname,
			DeleteBucketResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.deleteBucket(new DeleteBucketRequest(bucketname), resultHandler);
	}

	@Override
	public void deleteBucket(DeleteBucketRequest request,
			DeleteBucketResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		invoke(auth, request, resultHandler);
	}

	/* Object */
	@Override
	public void listObjects(String bucketname,
			ListObjectsResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.listObjects(new ListObjectsRequest(bucketname), resultHandler);
	}

	@Override
	public void listObjects(String bucketname, String prefix,
			ListObjectsResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.listObjects(new ListObjectsRequest(bucketname, prefix),
				resultHandler);
	}

	@Override
	public void listObjects(ListObjectsRequest request,
			ListObjectsResponseHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public void deleteObject(String bucketname, String key,
			DeleteObjectRequestHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.deleteObject(new DeleteObjectRequest(bucketname, key), handler);
	}

	@Override
	public void deleteObject(DeleteObjectRequest request,
			DeleteObjectRequestHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, handler);

	}

	@Override
	public void getObject(Context context, String bucketname, String key,
			GetObjectResponceHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.context = context;
		this.getObject(new GetObjectRequest(bucketname, key), handler);
	}

	@Override
	public void getObject(GetObjectRequest request,
			GetObjectResponceHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, handler);
	}

	@Override
	public void putObject(String bucketname, String objectkey, File file,
			PutObjectResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.putObject(new PutObjectRequest(bucketname, objectkey, file),
				handler);
	}

	@Override
	public void putObject(String bucketname, String objectkey,
			InputStream inputstream, ObjectMetadata objectmeta,
			PutObjectResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.putObject(new PutObjectRequest(bucketname, objectkey, inputstream,
				objectmeta), handler);
	}

	@Override
	public void putObject(PutObjectRequest request,
			PutObjectResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, handler);
	}

	@Override
	public void headObject(String bucketname, String objectkey,
			HeadObjectResponseHandler resultHandler) throws Ks3ClientException,
			Ks3ServiceException {
		this.headObject(new HeadObjectRequest(bucketname, objectkey),
				resultHandler);
	}

	@Override
	public void headObject(HeadObjectRequest request,
			HeadObjectResponseHandler resultHandler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public void copyObject(String destinationBucket, String destinationObject,
			String sourceBucket, String sourceKey,
			CopyObjectResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		CopyObjectRequest request = new CopyObjectRequest(destinationBucket,
				destinationObject, sourceBucket, sourceKey);
		this.copyObject(request, handler);

	}

	@Override
	public void copyObject(String destinationBucket, String destinationObject,
			String sourceBucket, String sourceKey,
			CannedAccessControlList cannedAcl, CopyObjectResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		CopyObjectRequest request = new CopyObjectRequest(destinationBucket,
				destinationObject, sourceBucket, sourceKey, cannedAcl);
		this.copyObject(request, handler);

	}

	@Override
	public void copyObject(String destinationBucket, String destinationObject,
			String sourceBucket, String sourceKey,
			AccessControlList accessControlList,
			CopyObjectResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {

		CopyObjectRequest request = new CopyObjectRequest(destinationBucket,
				destinationObject, sourceBucket, sourceKey, accessControlList);
		this.copyObject(request, handler);

	}

	@Override
	public void copyObject(CopyObjectRequest request,
			CopyObjectResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, handler);

	}

	/* MultiUpload */
	@Override
	public void initiateMultipartUpload(String bucketname, String objectkey,
			InitiateMultipartUploadResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.initiateMultipartUpload(new InitiateMultipartUploadRequest(
				bucketname, objectkey), resultHandler);
	}

	@Override
	public void initiateMultipartUpload(InitiateMultipartUploadRequest request,
			InitiateMultipartUploadResponceHandler resultHandler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public void uploadPart(String bucketName, String key, String uploadId,
			File file, long offset, int partNumber, long partSize,
			UploadPartResponceHandler resultHandler) throws Ks3ClientException,
			Ks3ServiceException {
		this.uploadPart(new UploadPartRequest(bucketName, key, uploadId, file,
				offset, partNumber, partSize), resultHandler);
	}

	@Override
	public void uploadPart(UploadPartRequest request,
			UploadPartResponceHandler resultHandler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, resultHandler);
	}

	@Override
	public void completeMultipartUpload(ListPartsResult result,
			CompleteMultipartUploadResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.completeMultipartUpload(
				new CompleteMultipartUploadRequest(result), handler);
	}

	@Override
	public void completeMultipartUpload(String bucketname, String objectkey,
			String uploadId, List<PartETag> partETags,
			CompleteMultipartUploadResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.completeMultipartUpload(new CompleteMultipartUploadRequest(
				bucketname, objectkey, uploadId, partETags), handler);
	}

	@Override
	public void completeMultipartUpload(CompleteMultipartUploadRequest request,
			CompleteMultipartUploadResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, handler);
	}

	@Override
	public void abortMultipartUpload(String bucketname, String objectkey,
			String uploadId, AbortMultipartUploadResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.abortMultipartUpload(new AbortMultipartUploadRequest(bucketname,
				objectkey, uploadId), handler);
	}

	@Override
	public void abortMultipartUpload(AbortMultipartUploadRequest request,
			AbortMultipartUploadResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.invoke(auth, request, handler);
	}

	@Override
	public void listParts(String bucketname, String objectkey, String uploadId,
			ListPartsResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.listParts(new ListPartsRequest(bucketname, objectkey, uploadId),
				handler);
	}

	@Override
	public void listParts(String bucketname, String objectkey, String uploadId,
			int maxParts, ListPartsResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.listParts(new ListPartsRequest(bucketname, objectkey, uploadId,
				maxParts), handler);
	}

	@Override
	public void listParts(String bucketname, String objectkey, String uploadId,
			int maxParts, int partNumberMarker, ListPartsResponseHandler handler)
			throws Ks3ClientException, Ks3ServiceException {
		this.listParts(new ListPartsRequest(bucketname, objectkey, uploadId,
				maxParts, partNumberMarker), handler);
	}

	@Override
	public void listParts(ListPartsRequest request,
			ListPartsResponseHandler handler) throws Ks3ClientException,
			Ks3ServiceException {
		this.invoke(auth, request, handler);
	}

	/* Invoke asnyc http client */
	private void invoke(Authorization auth, Ks3HttpRequest request,
			AsyncHttpResponseHandler resultHandler) {
		client.invoke(auth, request, resultHandler, clientConfiguration,
				context, endpoint, authListener, isUseAsyncMode);
	}

	@Override
	public void pause(Context context) {
		client.pause(context);
	}

	@Override
	public void cancel(Context context) {
		client.cancel(context);
	}

	@Override
	public Context getContext() {
		return this.context;
	}

	public Boolean getIsUseAsyncMode() {
		return isUseAsyncMode;
	}

	public void setIsUseAsyncMode(Boolean isUseAsyncMode) {
		this.isUseAsyncMode = isUseAsyncMode;
	}

	public ArrayList<Bucket> syncListBuckets() throws Throwable {
		final ArrayList<Bucket> list = new ArrayList<Bucket>();

		final Throwable error = new Throwable();
		this.listBuckets(new ListBucketsResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					ArrayList<Bucket> resultList) {
				list.addAll(resultList);
			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);
			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return list;
	}

	public AccessControlPolicy syncGetBucketACL(String bucketName)
			throws Throwable {

		final AccessControlPolicy policy = new AccessControlPolicy();
		final Throwable error = new Throwable();
		this.getBucketACL(bucketName, new GetBucketACLResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					AccessControlPolicy accessControlPolicy) {
				policy.setAccessControlList(accessControlPolicy
						.getAccessControlList());
				policy.setGrants(accessControlPolicy.getGrants());
				policy.setOwner(accessControlPolicy.getOwner());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);
			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return policy;
	}

	public AccessControlPolicy syncGetBucketACL(GetBucketACLRequest request)
			throws Throwable {

		final AccessControlPolicy policy = new AccessControlPolicy();
		final Throwable error = new Throwable();
		this.getBucketACL(request, new GetBucketACLResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					AccessControlPolicy accessControlPolicy) {
				policy.setAccessControlList(accessControlPolicy
						.getAccessControlList());
				policy.setGrants(accessControlPolicy.getGrants());
				policy.setOwner(accessControlPolicy.getOwner());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return policy;
	}

	public void syncPutBucketACL(String bucketName,
			AccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.putBucketACL(bucketName, accessControlList,
				new PutBucketACLResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncPutBucketACL(String bucketName,
			CannedAccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.putBucketACL(bucketName, accessControlList,
				new PutBucketACLResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);
					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncPutBucketACL(PutBucketACLRequest request) throws Throwable {

		final Throwable error = new Throwable();
		this.putBucketACL(request, new PutBucketACLResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);
			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncPutObjectACL(String bucketName, String objectKey,
			CannedAccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.putObjectACL(bucketName, objectKey, accessControlList,
				new PutObjectACLResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);
					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncPutObjectACL(String bucketName, String objectKey,
			AccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.putObjectACL(bucketName, objectKey, accessControlList,
				new PutObjectACLResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);
					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncPutObjectACL(PutObjectACLRequest request,
			AccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.putObjectACL(request, new PutObjectACLResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public AccessControlPolicy syncGetObjectACL(String bucketName,
			String ObjectKey) throws Throwable {

		final AccessControlPolicy policy = new AccessControlPolicy();
		final Throwable error = new Throwable();
		this.getObjectACL(bucketName, ObjectKey,
				new GetObjectACLResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders,
							AccessControlPolicy accessControlPolicy) {
						policy.setAccessControlList(accessControlPolicy
								.getAccessControlList());
						policy.setGrants(accessControlPolicy.getGrants());
						policy.setOwner(accessControlPolicy.getOwner());
					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return policy;
	}

	public AccessControlPolicy syncGetObjectACL(GetObjectACLRequest request)
			throws Throwable {

		final AccessControlPolicy policy = new AccessControlPolicy();
		final Throwable error = new Throwable();
		this.getObjectACL(request, new GetObjectACLResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					AccessControlPolicy accessControlPolicy) {
				policy.setAccessControlList(accessControlPolicy
						.getAccessControlList());
				policy.setGrants(accessControlPolicy.getGrants());
				policy.setOwner(accessControlPolicy.getOwner());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return policy;
	}

	public void syncHeadBucket(String bucketName) throws Throwable {

		final Throwable error = new Throwable();
		this.headBucket(bucketName, new HeadBucketResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncHeadBucket(HeadBucketRequest request) throws Throwable {

		final Throwable error = new Throwable();
		this.headBucket(request, new HeadBucketResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncCreateBucket(String bucketName) throws Throwable {

		final Throwable error = new Throwable();
		this.createBucket(bucketName, new CreateBucketResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncCreateBucket(String bucketName,
			CannedAccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.createBucket(bucketName, accessControlList,
				new CreateBucketResponceHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncCreateBucket(String bucketName,
			AccessControlList accessControlList) throws Throwable {

		final Throwable error = new Throwable();
		this.createBucket(bucketName, accessControlList,
				new CreateBucketResponceHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncCreateBucket(CreateBucketRequest request) throws Throwable {

		final Throwable error = new Throwable();
		this.createBucket(request, new CreateBucketResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncDeleteBucket(String bucketName) throws Throwable {

		final Throwable error = new Throwable();
		this.deleteBucket(bucketName, new DeleteBucketResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncDeleteBucket(DeleteBucketRequest request) throws Throwable {

		final Throwable error = new Throwable();
		this.deleteBucket(request, new DeleteBucketResponceHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public ObjectListing syncListObject(String bucketName) throws Throwable {

		final ObjectListing listing = new ObjectListing();
		final Throwable error = new Throwable();
		this.listObjects(bucketName, new ListObjectsResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					ObjectListing objectListing) {
				listing.setBucketName(objectListing.getBucketName());
				listing.setCommonPrefixes(objectListing.getCommonPrefixes());
				listing.setDelimiter(objectListing.getDelimiter());
				listing.setMarker(objectListing.getMarker());
				listing.setMaxKeys(objectListing.getMaxKeys());
				listing.setNextMarker(objectListing.getNextMarker());
				listing.setObjectSummaries(objectListing.getObjectSummaries());
				listing.setPrefix(objectListing.getPrefix());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return listing;
	}

	public ObjectListing syncListObject(String bucketName, String prefix)
			throws Throwable {

		final ObjectListing listing = new ObjectListing();
		final Throwable error = new Throwable();
		this.listObjects(bucketName, prefix, new ListObjectsResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					ObjectListing objectListing) {
				listing.setBucketName(objectListing.getBucketName());
				listing.setCommonPrefixes(objectListing.getCommonPrefixes());
				listing.setDelimiter(objectListing.getDelimiter());
				listing.setMarker(objectListing.getMarker());
				listing.setMaxKeys(objectListing.getMaxKeys());
				listing.setNextMarker(objectListing.getNextMarker());
				listing.setObjectSummaries(objectListing.getObjectSummaries());
				listing.setPrefix(objectListing.getPrefix());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return listing;
	}

	public ObjectListing syncListObject(ListObjectsRequest request)
			throws Throwable {

		final ObjectListing listing = new ObjectListing();
		final Throwable error = new Throwable();
		this.listObjects(request, new ListObjectsResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					ObjectListing objectListing) {
				listing.setBucketName(objectListing.getBucketName());
				listing.setCommonPrefixes(objectListing.getCommonPrefixes());
				listing.setDelimiter(objectListing.getDelimiter());
				listing.setMarker(objectListing.getMarker());
				listing.setMaxKeys(objectListing.getMaxKeys());
				listing.setNextMarker(objectListing.getNextMarker());
				listing.setObjectSummaries(objectListing.getObjectSummaries());
				listing.setPrefix(objectListing.getPrefix());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return listing;
	}

	public void syncDeleteObject(String bucketName, String objectKey)
			throws Throwable {

		final Throwable error = new Throwable();
		this.deleteObject(bucketName, objectKey,
				new DeleteObjectRequestHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncDeleteObject(DeleteObjectRequest request) throws Throwable {

		final Throwable error = new Throwable();
		this.deleteObject(request, new DeleteObjectRequestHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders) {

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public HeadObjectResult syncHeadObject(String bucketName, String objectKey)
			throws Throwable {

		final HeadObjectResult result = new HeadObjectResult();
		final Throwable error = new Throwable();
		this.headObject(bucketName, objectKey, new HeadObjectResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					HeadObjectResult headObjectResult) {
				result.setETag(headObjectResult.getETag());
				result.setLastmodified(headObjectResult.getLastmodified());
				result.setObjectMetadata(headObjectResult.getObjectMetadata());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return result;
	}

	public HeadObjectResult syncHeadObject(HeadObjectRequest request)
			throws Throwable {

		final HeadObjectResult result = new HeadObjectResult();
		final Throwable error = new Throwable();
		this.headObject(request, new HeadObjectResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					HeadObjectResult headObjectResult) {
				result.setETag(headObjectResult.getETag());
				result.setLastmodified(headObjectResult.getLastmodified());
				result.setObjectMetadata(headObjectResult.getObjectMetadata());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return result;
	}

	public CopyResult syncCopyObject(String destinationBucket,
			String destinationObject, String sourceBucket, String sourceKey)
			throws Throwable {

		final CopyResult copyResult = new CopyResult();
		final Throwable error = new Throwable();
		this.copyObject(destinationBucket, destinationObject, sourceBucket,
				sourceKey, new CopyObjectResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders, CopyResult result) {
						copyResult.setETag(result.getETag());
						copyResult.setLastModified(result.getLastModified());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return copyResult;
	}

	public CopyResult syncCopyObject(String destinationBucket,
			String destinationObject, String sourceBucket, String sourceKey,
			CannedAccessControlList accessControlList) throws Throwable {

		final CopyResult copyResult = new CopyResult();
		final Throwable error = new Throwable();
		this.copyObject(destinationBucket, destinationObject, sourceBucket,
				sourceKey, accessControlList, new CopyObjectResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders, CopyResult result) {
						copyResult.setETag(result.getETag());
						copyResult.setLastModified(result.getLastModified());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return copyResult;
	}

	public CopyResult syncCopyObject(String destinationBucket,
			String destinationObject, String sourceBucket, String sourceKey,
			AccessControlList accessControlList) throws Throwable {

		final CopyResult copyResult = new CopyResult();
		final Throwable error = new Throwable();
		this.copyObject(destinationBucket, destinationObject, sourceBucket,
				sourceKey, accessControlList, new CopyObjectResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders, CopyResult result) {
						copyResult.setETag(result.getETag());
						copyResult.setLastModified(result.getLastModified());
					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return copyResult;
	}

	public CopyResult syncCopyObject(CopyObjectRequest request)
			throws Throwable {

		final CopyResult copyResult = new CopyResult();
		final Throwable error = new Throwable();
		this.copyObject(request, new CopyObjectResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					CopyResult result) {
				copyResult.setETag(result.getETag());
				copyResult.setLastModified(result.getLastModified());

			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);

			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return copyResult;
	}

	public InitiateMultipartUploadResult syncInitiateMultipartUpload(
			String bucketName, String objectKey) throws Throwable {
		final InitiateMultipartUploadResult initResult = new InitiateMultipartUploadResult();
		final Throwable error = new Throwable();
		this.initiateMultipartUpload(bucketName, objectKey,
				new InitiateMultipartUploadResponceHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders,
							InitiateMultipartUploadResult result) {
						initResult.setBucket(result.getBucket());
						initResult.setKey(result.getKey());
						initResult.setUploadId(result.getUploadId());
					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return initResult;
	}

	public InitiateMultipartUploadResult syncInitiateMultipartUpload(
			InitiateMultipartUploadRequest request) throws Throwable {

		final InitiateMultipartUploadResult initResult = new InitiateMultipartUploadResult();
		final Throwable error = new Throwable();
		this.initiateMultipartUpload(request,
				new InitiateMultipartUploadResponceHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders,
							InitiateMultipartUploadResult result) {
						initResult.setBucket(result.getBucket());
						initResult.setKey(result.getKey());
						initResult.setUploadId(result.getUploadId());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return initResult;
	}

	public CompleteMultipartUploadResult syncCompleteMultipartUpload(
			String bucketName, String ObjectKey, String UploadId,
			List<PartETag> partETags) throws Throwable {

		final CompleteMultipartUploadResult completeMultipartUploadResult = new CompleteMultipartUploadResult();
		final Throwable error = new Throwable();
		this.completeMultipartUpload(bucketName, ObjectKey, UploadId,
				partETags, new CompleteMultipartUploadResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders,
							CompleteMultipartUploadResult result) {
						completeMultipartUploadResult.setBucket(result
								.getBucket());
						completeMultipartUploadResult.setKey(result.getKey());
						completeMultipartUploadResult.seteTag(result.geteTag());
						completeMultipartUploadResult.setLocation(result
								.getLocation());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return completeMultipartUploadResult;
	}

	public CompleteMultipartUploadResult syncCompleteMultipartUpload(
			ListPartsResult result) throws Throwable {

		final CompleteMultipartUploadResult completeMultipartUploadResult = new CompleteMultipartUploadResult();
		final Throwable error = new Throwable();
		this.completeMultipartUpload(result,
				new CompleteMultipartUploadResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders,
							CompleteMultipartUploadResult result) {
						completeMultipartUploadResult.setBucket(result
								.getBucket());
						completeMultipartUploadResult.setKey(result.getKey());
						completeMultipartUploadResult.seteTag(result.geteTag());
						completeMultipartUploadResult.setLocation(result
								.getLocation());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return completeMultipartUploadResult;
	}

	public CompleteMultipartUploadResult syncCompleteMultipartUpload(
			CompleteMultipartUploadRequest request) throws Throwable {

		final CompleteMultipartUploadResult completeMultipartUploadResult = new CompleteMultipartUploadResult();
		final Throwable error = new Throwable();
		this.completeMultipartUpload(request,
				new CompleteMultipartUploadResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders,
							CompleteMultipartUploadResult result) {
						completeMultipartUploadResult.setBucket(result
								.getBucket());
						completeMultipartUploadResult.setKey(result.getKey());
						completeMultipartUploadResult.seteTag(result.geteTag());
						completeMultipartUploadResult.setLocation(result
								.getLocation());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return completeMultipartUploadResult;
	}

	public void syncAbortMultipartUpload(AbortMultipartUploadRequest request)
			throws Throwable {

		final Throwable error = new Throwable();
		this.abortMultipartUpload(request,
				new AbortMultipartUploadResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public void syncAbortMultipartUpload(String bucketname, String objectKey,
			String uploadId) throws Throwable {

		final Throwable error = new Throwable();
		this.abortMultipartUpload(bucketname, objectKey, uploadId,
				new AbortMultipartUploadResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders) {

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
	}

	public ListPartsResult syncListParts(String bucketName, String objectKey,
			String uploadId) throws Throwable {

		final ListPartsResult listPartsResult = new ListPartsResult();
		final Throwable error = new Throwable();
		this.listParts(bucketName, objectKey, uploadId,
				new ListPartsResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders, ListPartsResult result) {
						listPartsResult.setBucketname(result.getBucketname());
						listPartsResult.setEncodingType(result
								.getEncodingType());
						listPartsResult.setInitiator(result.getInitiator());
						listPartsResult.setKey(result.getKey());
						listPartsResult.setMaxParts(result.getMaxParts());
						listPartsResult.setNextPartNumberMarker(result
								.getNextPartNumberMarker());
						listPartsResult.setOwner(result.getOwner());
						listPartsResult.setPartNumberMarker(result
								.getPartNumberMarker());
						listPartsResult.setParts(result.getParts());
						listPartsResult.setUploadId(result.getUploadId());
					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);

					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return listPartsResult;
	}

	public ListPartsResult syncListParts(String bucketName, String objectKey,
			String uploadId, int maxParts) throws Throwable {

		final ListPartsResult listPartsResult = new ListPartsResult();
		final Throwable error = new Throwable();
		this.listParts(bucketName, objectKey, uploadId, maxParts,
				new ListPartsResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders, ListPartsResult result) {
						listPartsResult.setBucketname(result.getBucketname());
						listPartsResult.setEncodingType(result
								.getEncodingType());
						listPartsResult.setInitiator(result.getInitiator());
						listPartsResult.setKey(result.getKey());
						listPartsResult.setMaxParts(result.getMaxParts());
						listPartsResult.setNextPartNumberMarker(result
								.getNextPartNumberMarker());
						listPartsResult.setOwner(result.getOwner());
						listPartsResult.setPartNumberMarker(result
								.getPartNumberMarker());
						listPartsResult.setParts(result.getParts());
						listPartsResult.setUploadId(result.getUploadId());

					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);
					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return listPartsResult;
	}

	public ListPartsResult syncListParts(String bucketName, String objectKey,
			String uploadId, int maxParts, int partNumberMarker)
			throws Throwable {

		final ListPartsResult listPartsResult = new ListPartsResult();
		final Throwable error = new Throwable();
		this.listParts(bucketName, objectKey, uploadId, maxParts,
				partNumberMarker, new ListPartsResponseHandler() {

					@Override
					public void onSuccess(int statesCode,
							Header[] responceHeaders, ListPartsResult result) {
						listPartsResult.setBucketname(result.getBucketname());
						listPartsResult.setEncodingType(result
								.getEncodingType());
						listPartsResult.setInitiator(result.getInitiator());
						listPartsResult.setKey(result.getKey());
						listPartsResult.setMaxParts(result.getMaxParts());
						listPartsResult.setNextPartNumberMarker(result
								.getNextPartNumberMarker());
						listPartsResult.setOwner(result.getOwner());
						listPartsResult.setPartNumberMarker(result
								.getPartNumberMarker());
						listPartsResult.setParts(result.getParts());
						listPartsResult.setUploadId(result.getUploadId());
					}

					@Override
					public void onFailure(int statesCode,
							Header[] responceHeaders, String response,
							Throwable paramThrowable) {
						error.initCause(paramThrowable);
					}
				});
		if (error.getCause() != null) {
			throw error;
		}
		return listPartsResult;
	}

	public ListPartsResult syncListParts(ListPartsRequest request)
			throws Throwable {
		final ListPartsResult listPartsResult = new ListPartsResult();
		final Throwable error = new Throwable();
		this.listParts(request, new ListPartsResponseHandler() {

			@Override
			public void onSuccess(int statesCode, Header[] responceHeaders,
					ListPartsResult result) {
				listPartsResult.setBucketname(result.getBucketname());
				listPartsResult.setEncodingType(result.getEncodingType());
				listPartsResult.setInitiator(result.getInitiator());
				listPartsResult.setKey(result.getKey());
				listPartsResult.setMaxParts(result.getMaxParts());
				listPartsResult.setNextPartNumberMarker(result
						.getNextPartNumberMarker());
				listPartsResult.setOwner(result.getOwner());
				listPartsResult.setPartNumberMarker(result
						.getPartNumberMarker());
				listPartsResult.setParts(result.getParts());
				listPartsResult.setUploadId(result.getUploadId());
			}

			@Override
			public void onFailure(int statesCode, Header[] responceHeaders,
					String response, Throwable paramThrowable) {
				error.initCause(paramThrowable);
			}
		});
		if (error.getCause() != null) {
			throw error;
		}
		return listPartsResult;
	}
}
