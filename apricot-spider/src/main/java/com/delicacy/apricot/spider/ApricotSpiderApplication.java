package com.delicacy.apricot.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

@SpringBootApplication
public class ApricotSpiderApplication {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
            args = new String[]{"-ttjj","fundrank"};
//            args = new String[]{"-ttjj","fundposition"};
//            args = new String[]{"-xmly","3965403"};
//           args = new String[]{"-xq","cn"};
//            args = new String[]{"-xq","cn_report"};
		}
		Random rand=new Random();
		int port = rand.nextInt(5000) + 5000;
		System.getProperties().put( "server.port", port );
		SpringApplication.run(ApricotSpiderApplication.class, args);
	}

}
