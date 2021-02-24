package indi.atlantis.framework.seafloor.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.github.paganini2008.devtools.io.FileUtils;
import com.github.paganini2008.devtools.io.IOUtils;

/**
 * 
 * RestTemplateDownloadHandler
 * 
 * @author Jimmy Hoff
 *
 * @version 1.0
 */
public class RestTemplateDownloadHandler implements RestTemplateCallback<File> {

	private final File dir;
	private final String fileName;

	public RestTemplateDownloadHandler(File dir, String fileName) {
		this.dir = dir;
		this.fileName = fileName;
	}

	@Override
	public RequestCallback getRequestCallback(RestTemplate restTemplate) {
		return null;
	}

	@Override
	public ResponseExtractor<File> getResponseExtractor(RestTemplate restTemplate) {
		return response -> {
			File tmpFile = FileUtils.getFile(dir, fileName);
			OutputStream output = new FileOutputStream(tmpFile);
			try {
				IOUtils.copy(response.getBody(), output);
			} finally {
				IOUtils.closeQuietly(output);
			}
			return tmpFile;
		};
	}

}
