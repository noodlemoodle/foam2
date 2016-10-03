package foam.core;

public class SimpleFacetManager implements FacetManager {
  public Object getInstanceOf(Object value, Class type, X x) {
    return create(type, x);
  }

  public Object create(Class type, X x) {
    try {
      // Automatically load FooImpl if Foo is abstract.
      if ( java.lang.reflect.Modifier.isAbstract(type.getModifiers()) ) {
        type = Class.forName(type.getName() + "Impl");
      }

      Object obj = type.newInstance();

      if ( obj instanceof ContextAware ) {
        ((ContextAware)obj).setX(x);
      }

      return obj;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
