package indi.atlantis.framework.seafloor.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.springframework.http.MediaType;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.devtools.io.IOUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestTemplateDownloadHandler
 * 
 * @author Jimmy Hoff
 *
 * @version 1.0
 */
@Slf4j
public class RestTemplateDownloadHandler implements RestTemplateCallback<File> {

	private final File dir;
	private final String fileName;

	public RestTemplateDownloadHandler(File dir, String fileName) {
		this.dir = dir;
		this.fileName = fileName;
	}

	@Override
	public RequestCallback getRequestCallback(RestTemplate restTemplate) {
		return request -> {
//			request.getHeaders().set("User-Agent",
//					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
			request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
		};
	}

	@Override
	public ResponseExtractor<File> getResponseExtractor(RestTemplate restTemplate) {
		return response -> {
			File tmpFile = FileUtils.getFile(dir, fileName);
			OutputStream output = new FileOutputStream(tmpFile, false);
			try {
				long length = IOUtils.copy(response.getBody(), output);
				if (log.isTraceEnabled()) {
					log.trace("Downloaded file: {}, length: {}", tmpFile, length);
				}
			} finally {
				IOUtils.flushAndCloseQuietly(output);
			}
			return tmpFile;
		};
	}

}
