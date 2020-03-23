package fr.umlv.cheapcoverage;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public class RT {
  private static final MethodHandle PROBE;
  static {
    try {
      PROBE = lookup().findStatic(RT.class, "probe", methodType(void.class, String.class, int.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings("unused")
  private static void probe(String method, int line) {
    System.err.println("PROBE: called each time " + method + " at " + line);
  }

  @SuppressWarnings("unused")
  public static CallSite bsm(Lookup lookup, String name, MethodType type, String method, int line) {
    System.err.println("BSM: called from class " + lookup.lookupClass().getName());
    System.err.println("            from method " + method + " at line " + line);

    //return new ConstantCallSite(empty(type));    // return no-op
    return new ConstantCallSite(insertArguments(PROBE, 0, method, line));
  }
}
