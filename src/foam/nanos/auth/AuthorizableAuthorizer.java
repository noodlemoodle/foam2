/**
 * @license
 * Copyright 2018 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package foam.nanos.auth;

import foam.core.FObject;
import foam.core.X;

public class AuthorizableAuthorizer implements Authorizer {

  private String name;

  public AuthorizableAuthorizer(String name) {  
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void authorizeOnCreate(X x, FObject obj) throws AuthorizationException {
    if ( obj instanceof Authorizable ) {
      ((Authorizable) obj).authorizeOnCreate(x);
    }
  }

  public void authorizeOnRead(X x, FObject obj) throws AuthorizationException {
    if ( obj instanceof Authorizable ) {
      ((Authorizable) obj).authorizeOnRead(x);
    }
  }

  public void authorizeOnUpdate(X x, FObject oldObj, FObject obj) throws AuthorizationException {
    if ( obj instanceof Authorizable ) {
      ((Authorizable) obj).authorizeOnUpdate(x, oldObj);
    }
  }

  public void authorizeOnDelete(X x, FObject obj) throws AuthorizationException {
    if ( obj instanceof Authorizable ) {
      ((Authorizable) obj).authorizeOnDelete(x);
    }
  }

  public String createPermission(String op) {
    return name + "." + op;
  }

  public boolean checkGlobalRead(X x) {
    // String permission = createPermission("read");
    // AuthService authService = (AuthService) x.get("auth");
    // try {
    //   return authService.check(x, permission);
    // } catch ( Exception e ) {
    //   return false;
    // }
    return false;
  }

  public boolean checkGlobalRemove(X x) {
    // String permission = createPermission("remove");
    // AuthService authService = (AuthService) x.get("auth");
    // try {
    //   return authService.check(x, permission);
    // } catch ( Exception e ) {
    //   return false;
    // }
    return false;
  }
}
