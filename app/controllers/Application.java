package controllers;

import models.Usr;
import models.utils.AppException;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import static controllers.form.Form.Login;
import static controllers.form.Form.Register;

import static play.data.Form.form;

/**
 * Login and Logout.
 */
public class Application extends Controller {

    public static Result GO_HOME = redirect(
            routes.Application.index()
    );

    public static Result GO_DASHBOARD = redirect(
            routes.Dashboard.index()
    );

    /**
     * Display the login page or dashboard if connected
     *
     * @return login page or dashboard
     */
    public static Result index() {
        // Check that the email matches a confirmed user before we redirect
        String email = ctx().session().get("email");
        if (email != null) {
            Usr user = Usr.findByEmail(email);
            if (user != null && user.validated) {
                return GO_DASHBOARD;
            } else {
                Logger.debug("Clearing invalid session credentials");
                session().clear();
            }
        }

        return ok(index.render(views.html.login.render(form(Login.class)),views.html.profile.render()));
    }
    public static Result login() {
        return ok(views.html.login.render(form(Login.class)));
    }


    /**
     * Handle login form submission.
     *
     * @return Dashboard if auth OK or login form if auth KO
     */
    public static Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        Logger.info("Trying to authenticate user");
        Form<Register> registerForm = form(Register.class);

        if (loginForm.hasErrors()) {
            Logger.info("Error while Trying to authenticate user");
         return    badRequest(index.render(views.html.login.render(form(Login.class)),
                 views.html.profile.render()));
//            return badRequest(index.render(null,null));
        } else {
            session("email", loginForm.get().getEmail());
            return GO_DASHBOARD;
        }
    }

    /**
     * Logout and clean the session.
     *
     * @return Index page
     */
    public static Result logout() {
        session().clear();
        flash("success", Messages.get("youve.been.logged.out"));
        return GO_HOME;
    }

}