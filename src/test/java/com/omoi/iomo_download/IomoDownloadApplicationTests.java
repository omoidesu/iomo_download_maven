package com.omoi.iomo_download;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.omoi.iomo_download.entity.dto.OsuCookie;
import com.omoi.iomo_download.service.OsuService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class IomoDownloadApplicationTests {
	@Autowired
	private OsuService osuService;

	//@Test
	//void testLogin() {
	//	OsuCookie cookie = osuService.login();
	//	assert cookie != null;
	//}

	@Test
	void loginTest() {
		HttpResponse execute = HttpRequest
				.get("https://osu.ppy.sh/home")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
				.execute();
		log.info("code: {}", execute.getStatus());
	}

	@Test
	void downloadTest() {
		String path = osuService.downloadMap("1054931");
		log.info(path);
		Assertions.assertNotNull(path);
	}

	@Test
	void uploadMusicTest() {
		String kookPath = osuService.uploadMusic("1017271", "audio.mp3");
		log.info(kookPath);
	}

	@Test
	void zipFiles() {
		String path = osuService.createBpPack("test", List.of("503213", "1017271", "2103242", "1054931"));
		log.info(path);
	}
}
