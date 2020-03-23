package fr.umlv.cheapcoverage;

import static java.lang.constant.ConstantDescs.CD_CallSite;
import static java.lang.constant.ConstantDescs.CD_MethodHandles_Lookup;
import static java.lang.constant.ConstantDescs.CD_MethodType;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class Main {
  private static final Handle BSM = new Handle(H_INVOKESTATIC,
      RT.class.getName().replace('.', '/'),
      "bsm",
      MethodTypeDesc.of(CD_CallSite, CD_MethodHandles_Lookup, CD_String, CD_MethodType, CD_String, CD_int).descriptorString(),
      false);

  public static void main(String[] args)
      throws Throwable {
    byte[] data;
    try(var input = Files.newInputStream(Path.of("Calculator.class"))) {
      data = input.readAllBytes();
    }
    var reader = new ClassReader(data);
    var writer = new ClassWriter(reader, COMPUTE_FRAMES);
    reader.accept(
        new ClassVisitor(ASM7, writer) {
          @Override
          public MethodVisitor visitMethod(
              int access, String name, String descriptor, String signature, String[] exceptions) {
            var mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MethodVisitor(ASM7, mv) {
              @Override
              public void visitLineNumber(int line, Label start) {
                super.visitInvokeDynamicInsn("probe", "()V", BSM, name + descriptor, line);
                super.visitLineNumber(line, start);
              }
            };
          }
        }, 0);

    var bytecode = writer.toByteArray();
    var clazz = new ClassLoader() {
      private Class<?> define() {
        return defineClass(null, bytecode, 0, bytecode.length);
      }
    }.define();

    var mh = lookup().findStatic(clazz, "sum", methodType(int.class, int.class, int.class));
    var sum = (int)mh.invokeExact(2, 3);
    System.out.println("sum: " + sum);
  }
}
