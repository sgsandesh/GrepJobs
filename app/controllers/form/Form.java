package controllers.form;

import models.Usr;
import models.utils.AppException;
import play.Logger;
import play.data.validation.Constraints;
import play.i18n.Messages;

public  class Form {
    public static class Login {

        @Constraints.Required
        private String email;
        @Constraints.Required
        private String password;

        /**
         * Validate the authentication.
         *
         * @return null if validation ok, string with details otherwise
         */
        public String validate() {

            Usr user = null;
            try {
                user = Usr.authenticate(email, password);

            } catch (AppException e) {
                return Messages.get("error.technical");
            }
            if (user == null) {
                Logger.info("Invalid user with email"+email);
                return Messages.get("invalid.user.or.password");
            } else if (!user.validated) {
                return Messages.get("account.not.validated.check.mail");
            }
            Logger.info("User with email"+email+" logged in successfully");
            return null;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
    public static class Register {

        @Constraints.Required
        public String email;

        @Constraints.Required
        public String fullname;

        @Constraints.Required
        public String inputPassword;

        /**
         * Validate the authentication.
         *
         * @return null if validation ok, string with details otherwise
         */
        public String validate() {
            if (isBlank(email)) {
                return "Email is required";
            }

            if (isBlank(fullname)) {
                return "Full name is required";
            }

            if (isBlank(inputPassword)) {
                return "Password is required";
            }

            return null;
        }

        private boolean isBlank(String input) {
            return input == null || input.isEmpty() || input.trim().isEmpty();
        }
    }

}



