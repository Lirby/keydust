package keydust.contollers;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PwdGeneratorController {
        private static final SecureRandom random = new SecureRandom();

        private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final String NUMB = "0123456789";
        private static final String SPEC = "!@#$%^&*()-_=+[]{}";

        private static final int MIN_LENGTH = 8;

        public static String generatePassword(int length,
                                              boolean useLower,
                                              boolean useUpper,
                                              boolean useNumb,
                                              boolean useSpec) {

            if (length < MIN_LENGTH ) {
                throw new IllegalArgumentException("Pasword must be at least" + MIN_LENGTH + "characters long.");
            }

            StringBuilder pool = new StringBuilder();
            StringBuilder password = new StringBuilder();

            if(useLower) {
                pool.append(LOWER);
                password.append(randomChar(pool.toString()));
            }

            if(useUpper) {
                pool.append(UPPER);
                password.append(randomChar(pool.toString()));
            }

            if(useNumb) {
                pool.append(NUMB);
                password.append(randomChar(pool.toString()));
            }

            if(useSpec) {
                pool.append(SPEC);
                password.append(randomChar(pool.toString()));
            }

            if(pool.length() == 0) {
                throw new IllegalArgumentException("At least one character type must be selected.");
            }

            while(password.length() < length) {
                password.append(randomChar(pool.toString()));
            }

            return shuffle(password.toString());
        }

        private static char randomChar(String chars) {
            return chars.charAt(random.nextInt(chars.length()));
        }
        private static String shuffle(String input) {
            List<String> result = Arrays.asList(input.split(""));
            Collections.shuffle(result);
            return String.join("", result);
        }
}
