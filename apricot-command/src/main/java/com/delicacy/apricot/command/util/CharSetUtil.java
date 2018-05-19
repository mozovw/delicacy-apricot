package com.delicacy.apricot.command.util;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.util.StringUtils;

import java.io.InputStream;


@Slf4j
public class CharSetUtil {


    public static String getCharset(InputStream is) {

        UniversalDetector detector = new UniversalDetector(null);
        try {
            byte[] bytes = new byte[1024];
            int nread;
            if ((nread = is.read(bytes)) > 0 && !detector.isDone()) {
                detector.handleData(bytes, 0, nread);
            }
        } catch (Exception localException) {
            log.info("detected code:", localException);
        }
        detector.dataEnd();
        String encode = detector.getDetectedCharset();
        /** default UTF-8 */
        if (StringUtils.isEmpty(encode)) {
            encode = "UTF-8";
        }
        detector.reset();
        return encode;
    }

}