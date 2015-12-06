package models;

import models.utils.AppException;
import models.utils.Hash;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class Usr extends Model {

    @Id
    public Long userId;

    @Constraints.Required
    @Formats.NonEmpty
    @Column(unique = true)
    public String email;

    @Constraints.Required
    @Formats.NonEmpty
    @Column(unique = true)
    public String fullname;

    public String confirmationToken;

    @Constraints.Required
    @Formats.NonEmpty
    public String passwordHash;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date dateCreation;

    @Formats.NonEmpty
    public Boolean validated = false;

    // -- Queries (long id, user.class)
    public static Model.Finder<Long, Usr> find = new Model.Finder<Long, Usr>(Long.class, Usr.class);

    /**
     * Retrieve a user from an email.
     *
     * @param email email to search
     * @return a user
     */
    public static Usr findByEmail(String email) {
        return find.where().eq("email", email).findUnique();
    }

    /**
     * Retrieve a user from a fullname.
     *
     * @param fullname Full name
     * @return a user
     */
    public static Usr findByFullname(String fullname) {
        return find.where().eq("fullname", fullname).findUnique();
    }

    /**
     * Retrieves a user from a confirmation token.
     *
     * @param token the confirmation token to use.
     * @return a user if the confirmation token is found, null otherwise.
     */
    public static Usr findByConfirmationToken(String token) {
        return find.where().eq("confirmationToken", token).findUnique();
    }

    /**
     * Authenticate a Usr, from a email and clear password.
     *
     * @param email         email
     * @param clearPassword clear password
     * @return Usr if authenticated, null otherwise
     * @throws AppException App Exception
     */
    public static Usr authenticate(String email, String clearPassword) throws AppException {

        // get the user with email only to keep the salt password
        Usr user = find.where().eq("email", email).findUnique();
        if (user != null) {
            // get the hash password from the salt + clear password
            if (Hash.checkPassword(clearPassword, user.passwordHash)) {
                return user;
            }
        }
        return null;
    }

    public void changePassword(String password) throws AppException {
        this.passwordHash = Hash.createPassword(password);
        this.save();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * Confirms an account.
     *
     * @return true if confirmed, false otherwise.
     * @throws AppException App Exception
     */
    public static boolean confirm(Usr user) throws AppException {
        if (user == null) {
            return false;
        }


        user.confirmationToken = null;
        user.validated = true;
        user.save();
        return true;
    }

}
