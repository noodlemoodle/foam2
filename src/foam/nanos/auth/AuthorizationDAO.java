/**
 * @license
 * Copyright 2018 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package foam.nanos.auth;

import foam.core.FObject;
import foam.core.InvalidX;
import foam.core.X;
import foam.dao.*;
import foam.mlang.order.Comparator;
import foam.mlang.predicate.Predicate;
import foam.nanos.auth.AuthorizationException;

import static foam.mlang.MLang.AND;
import static foam.mlang.MLang.HAS_PERMISSION;

/**
 * A DAO decorator to run authorization checks.
 */
public class AuthorizationDAO extends ProxyDAO {
  protected Authorizer authorizer_;
  protected boolean authorizedRead_;
  protected String name_;

  public AuthorizationDAO(X x, String name, DAO delegate) {
    this(x, name, true, delegate, StandardAuthorizer.instance(name));
  }

  public AuthorizationDAO(X x, String name, DAO delegate, Authorizer authorizer) {
    this(x, name, true, delegate, authorizer);
  }

  public AuthorizationDAO(X x, String name, boolean authorizedRead, DAO delegate) {
    this(x, name, authorizedRead, delegate, StandardAuthorizer.instance(name));
  }

  public AuthorizationDAO(X x, String name, boolean authorizedRead, DAO delegate, Authorizer authorizer) {
    AuthorizationException exception = new AuthorizationException("When " +
        "using a DAO decorated by AuthenticatedDAO, you may only call the " +
        "context-oriented methods: put_(), find_(), select_(), remove_(), " +
        "removeAll_(), pipe_(), and listen_(). Alternatively, you can also " +
        "use .inX() to set the context on the DAO.");
    setX(new InvalidX(exception));
    setDelegate(delegate);
    authorizer_ = authorizer;
    authorizedRead_ = authorizedRead;
    name_ = name;
  }

  @Override
  public FObject put_(X x, FObject obj) throws AuthorizationException {
    if ( obj == null ) throw new RuntimeException("Cannot put null.");

    Object id = obj.getProperty("id");
    FObject oldObj = getDelegate().find(id);
    boolean isCreate = id == null || oldObj == null;

    if ( isCreate ) {
      authorizer_.authorizeOnCreate(x, obj);
    } else {
      authorizer_.authorizeOnUpdate(x, oldObj, obj);
    } 

    return super.put_(x, obj);
  }

  @Override
  public FObject remove_(X x, FObject obj) {
    authorizer_.authorizeOnDelete(x, obj);
    return super.remove_(x, obj);
  }

  @Override
  public FObject find_(X x, Object id) {
    FObject obj = super.find_(x, id);
    if ( id == null || obj == null ) return null;
    if ( authorizedRead_ ) authorizer_.authorizeOnRead(x, obj);
    return obj;
  }

  @Override
  public Sink select_(X x, Sink sink, long skip, long limit, Comparator order, Predicate predicate) {
    // sink = ! authorizedRead_ || checkGlobalRead(x) ? sink : new AuthorizationSink(x, authorizer_, sink);
    if ( authorizedRead_ ) {
      super.select_(x, sink, skip, limit, order, augmentPredicate(x, predicate, "read"));
      return sink;
    }
    return super.select_(x, sink, skip, limit, order, predicate);
  }

  @Override
  public void removeAll_(X x, long skip, long limit, Comparator order, Predicate predicate) {
    this.select_(x, new RemoveSink(x, this), skip, limit, order, augmentPredicate(x, predicate, "delete"));
  }

  public boolean checkGlobalRead(X x) {
    // return false;

    AuthService auth = (AuthService) x.get("auth");
    return auth.check(x, name_ + ".read.*");
  }

  public boolean checkGlobalRemove(X x) {
    // return false;

    AuthService auth = (AuthService) x.get("auth");
    return auth.check(x, name_ + ".delete.*");
  }

  public Predicate augmentPredicate(X x, Predicate existingPredicate, String operation) {
    return existingPredicate != null ?
      AND(
        HAS_PERMISSION(x, name_ + "." + operation + ".*"),
        existingPredicate
      ) : 
      HAS_PERMISSION(x, name_ + "." + operation + ".*");
  }
}
