/**
 * @license
 * Copyright 2019 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package foam.nanos.auth;

import foam.core.FObject;
import foam.core.X;
import foam.nanos.auth.AuthService;
import foam.nanos.auth.AuthorizationException;

public class StandardAuthorizer implements Authorizer {

  private static StandardAuthorizer instance_ = null;
  protected String  name_;

  public static StandardAuthorizer instance(String name) {
    if ( instance_ == null ) {
      instance_ = new StandardAuthorizer(name);
    }
    return instance_;
  }

  private StandardAuthorizer(String name) {
    this.name_ = name;
  }

  public String createPermission(String op) {
    return name_ + "." + op + ".*";
  }

  public String createPermission(String op, Object id) {
    return name_ + "." + op + "." + id + ".*";
  }

  public void authorizeOnCreate(X x, FObject obj) throws AuthorizationException {

    String permission = createPermission("create");
    AuthService authService = (AuthService) x.get("auth");

    if ( ! authService.check(x, permission) ) {
      throw new AuthorizationException();
    }
  }

  public void authorizeOnRead(X x, FObject obj) throws AuthorizationException {

    Object id = obj.getProperty("id");
    String permission = createPermission("read", id);
    AuthService authService = (AuthService) x.get("auth");
    
    if ( ! authService.check(x, permission) ) {
      throw new AuthorizationException();
    }
  }

  public void authorizeOnUpdate(X x, FObject oldObj, FObject obj) throws AuthorizationException {

    Object id = oldObj.getProperty("id");
    String permission = createPermission("update", id);
    AuthService authService = (AuthService) x.get("auth");
    
    if ( ! authService.check(x, permission) ) {
      throw new AuthorizationException();
    }
  }

  public void authorizeOnDelete(X x, FObject obj) throws AuthorizationException {

    Object id = obj.getProperty("id");
    String permission  = createPermission("remove", id);
    AuthService authService = (AuthService) x.get("auth");
    
    if ( ! authService.check(x, permission) ) {
      throw new AuthorizationException();
    }
  }
}
