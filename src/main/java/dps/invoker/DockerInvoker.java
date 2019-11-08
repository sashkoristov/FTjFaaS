package dps.invoker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DockerInvoker implements FaaSInvoker {
	String result = null;
	boolean waiting = true;
	final static Logger logger = LoggerFactory.getLogger(DockerInvoker.class);

	/**
	 * Connects to docker client, creates the docker container, runs it and returns
	 * result as string.
	 */
	@Override
	public String invokeFunction(String function, Map<String, Object> parameters) throws Exception {
		String functionName = function.substring(nthLastIndexOf(2, "/", function) + 1);
		String dockerHost = function.substring(0, nthLastIndexOf(2, "/", function));
		String body = new Gson().toJson(parameters);
		DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost(dockerHost).build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		CreateContainerResponse containerResponse = null;

		// pull docker image if its not already on docker instance
		List<Image> images = dockerClient.listImagesCmd().exec();
		boolean imageAlreadyOnDocker = images.stream()
				.anyMatch(image -> Arrays.asList(image.getRepoTags()).contains(functionName));
		if (!imageAlreadyOnDocker) {
			PullImageResultCallback callback = new PullImageResultCallback();
			dockerClient.pullImageCmd(functionName).exec(callback);
			callback.awaitSuccess();
		}

		// create docker container on device
		if (!body.equals("{}")) {
			containerResponse = dockerClient.createContainerCmd(functionName).withCmd(body).withPrivileged(true).exec();

		} else {
			containerResponse = dockerClient.createContainerCmd(functionName).exec();
		}
		dockerClient.startContainerCmd(containerResponse.getId()).exec();
		WaitContainerResultCallback resultCallback = new WaitContainerResultCallback();
		dockerClient.waitContainerCmd(containerResponse.getId()).exec(resultCallback);

		try {
			dockerClient.logContainerCmd(containerResponse.getId()).withStdErr(true).withStdOut(true)
					.withFollowStream(true).exec(new ResultCallbackTemplate<LogContainerResultCallback, Frame>() {
						@Override
						public void onNext(Frame frame) {
							result = new String(frame.getPayload());
							waiting = false;
						}
					});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// wait for result and ping docker device every 5 minutes to maintain a stable
		// TCP connection to device
		int counter = 0;
		while (waiting) {
			try {
				if (counter == 300) {
					counter = 0;
					dockerClient.pingCmd().exec();
				}
				counter++;
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// remove stopped container from the device
		dockerClient.removeContainerCmd(containerResponse.getId()).exec();

		// OPTIONAL: remove image after execution
		// dockerClient.removeImageCmd(imageId);

		return result;

	}

	/**
	 * Helper function which is used to to split the function string into function
	 * name and host name. Returns the nth last index of a searched string.
	 * 
	 * @param nth    The number for n.
	 * @param ch     The character or string that is searched.
	 * @param string The string where the nth occurrence of the given string ch is
	 *               searched.
	 * @return The index of the nth occurrence.
	 */
	private int nthLastIndexOf(int nth, String ch, String string) {
		if (nth <= 0)
			return string.length();
		return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
	}

}
