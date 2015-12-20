package controllers.account;

import controllers.Application;
import models.Usr;
import models.utils.AppException;
import models.utils.Hash;
import models.utils.Mail;
import org.apache.commons.mail.EmailException;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.signup.confirm;
import views.html.account.signup.create;
import views.html.account.signup.created;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import static controllers.form.Form.Register;
import static play.data.Form.form;

/**
 * Signup to PlayStartApp : save and send confirm mail.
 * <p/>
 * 
 */
public class Signup extends Controller {

    /**
     * Display the create form.
     *
     * @return create form
     */
    public static Result create() {
        return ok(create.render(form(Register.class)));
    }

    /**
     * Display the create form only (for the index page).
     *
     * @return create form
     */
    public static Result createFormOnly() {
        return ok(create.render(form(Register.class)));
    }

    /**
     * Save the new user.
     *
     * @return Successfull page or created form if bad
     */
    public static Result save() {
        Form<Register> registerForm = form(Register.class).bindFromRequest();

        if (registerForm.hasErrors()) {
            return badRequest(create.render(registerForm));
        }

        Register register = registerForm.get();
        Result resultError = checkBeforeSave(registerForm, register.email);
        System.out.println("resultError = " + resultError);
        if (resultError != null) {
            return resultError;
        }
        System.out.println(" Trying to save the user = " );
        try {
            Usr user = new Usr();
            user.email = register.email;
            user.fullname = register.fullname;
            user.passwordHash = Hash.createPassword(register.inputPassword);
            user.confirmationToken = UUID.randomUUID().toString();

            user.save();
            sendMailAskForConfirmation(user);

            return ok(created.render());
        } catch (EmailException e) {
            Logger.debug("Signup.save Cannot send email", e);
            flash("error", Messages.get("error.sending.email"));
        } catch (Exception e) {
            Logger.error("Signup.save error", e);
            flash("error", Messages.get("error.technical"));
        }
        return badRequest(create.render(registerForm));
    }

    /**
     * Check if the email already exists.
     *
     * @param registerForm Usr Form submitted
     * @param email email address
     * @return Index if there was a problem, null otherwise
     */
    private static Result checkBeforeSave(Form<Register> registerForm, String email) {
        // Check unique email
        Usr user = Usr.findByEmail(email);
        if ( user!= null) {
            System.out.println("The user already exists with the email id"+user.fullname);
            System.out.println("registerForm = [" + registerForm + "], email = [" + email + "]");
            flash("error", Messages.get("error.email.already.exist"));
            return badRequest(create.render(registerForm));
        }

        return null;
    }

    /**
     * Send the welcome Email with the link to confirm.
     *
     * @param user user created
     * @throws EmailException Exception when sending mail
     */
    private static void sendMailAskForConfirmation(Usr user) throws EmailException, MalformedURLException {
        String subject = Messages.get("mail.confirm.subject");

        String urlString = "http://" + Configuration.root().getString("server.hostname");
        urlString += "/confirm/" + user.confirmationToken;
        URL url = new URL(urlString); // validate the URL, will throw an exception if bad.
        String message = Messages.get("mail.confirm.message", url.toString());

        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email);
        Mail.sendMail(envelop);
    }

    /**
     * Valid an account with the url in the confirm mail.
     *
     * @param token a token attached to the user we're confirming.
     * @return Confirmationpage
     */
    public static Result confirm(String token) {
        Usr user = Usr.findByConfirmationToken(token);
        if (user == null) {
            flash("error", Messages.get("error.unknown.email"));
            return badRequest(confirm.render());
        }

        if (user.validated) {
            flash("error", Messages.get("error.account.already.validated"));
            return badRequest(confirm.render());
        }

        try {
            if (Usr.confirm(user)) {
                sendMailConfirmation(user);
                flash("success", Messages.get("account.successfully.validated"));
                return ok(confirm.render());
            } else {
                Logger.debug("Signup.confirm cannot confirm user");
                flash("error", Messages.get("error.confirm"));
                return badRequest(confirm.render());
            }
        } catch (AppException e) {
            Logger.error("Cannot signup", e);
            flash("error", Messages.get("error.technical"));
        } catch (EmailException e) {
            Logger.debug("Cannot send email", e);
            flash("error", Messages.get("error.sending.confirm.email"));
        }
        return badRequest(confirm.render());
    }

    /**
     * Send the confirm mail.
     *
     * @param user user created
     * @throws EmailException Exception when sending mail
     */
    private static void sendMailConfirmation(Usr user) throws EmailException {
        String subject = Messages.get("mail.welcome.subject");
        String message = Messages.get("mail.welcome.message");
        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email);
        Mail.sendMail(envelop);
    }
}
