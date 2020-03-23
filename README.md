# cheapcoverage
A demo on how to gather code coverage information on Java (Kotlin, Scala, etc) classfiles

## Build
This project uses Maven
```
  mvn package
```

### Run
This project requires Java 14.
```
  java -jar target/cheapcoverage-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### How it works ?
There are two source files
- Main

  Load the classfile of Calculator.class as a byte array
  Transform it using [ASM](https://asm.ow2.io/) into another byte array
  The transformation add instructions __invokedynamic__ to the method RT.bsm() at each line of the code
  Define the byte array as a class
  Use a method handle to dynamically call the method sum of the new class
  Call the method handle

- RT

  Its method bsm is called once per instruction __invokedynamic__, the first time the VM try to execute it
  It return a method handle to the method `probe` that will be calls as many times as the instruction is executed

### The example of a stupid(TM) calculator
The following Java method
```java
  ...
  public static int sum(int a, int b) {
    var sum = a;
    for(var x = 0; x < b; x++) {
      sum++;
    }
    return sum;
  }
  ...
```
is compiled to
```
  public static int sum(int, int);
    Code:
       0: iload_0
       1: istore_2
       2: iconst_0
       3: istore_3
       4: iload_3
       5: iload_1
       6: if_icmpge     18
       9: iinc          2, 1
      12: iinc          3, 1
      15: goto          4
      18: iload_2
      19: ireturn
    LineNumberTable:
      line 3: 0
      line 4: 2
      line 5: 9
      line 4: 12
      line 7: 18
    StackMapTable: number_of_entries = 2
      frame_type = 253 /* append */
        offset_delta = 4
        locals = [ int, int ]
      frame_type = 250 /* chop */
        offset_delta = 13

```
The class file format is described in [section 4](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-4.html) of the Java Virtual Machine Specification.


### Output
The output is
```
BSM: called from class Calculator
            from method sum(II)I at line 3
PROBE: called each time sum(II)I at 3
BSM: called from class Calculator
            from method sum(II)I at line 4
PROBE: called each time sum(II)I at 4
BSM: called from class Calculator
            from method sum(II)I at line 5
PROBE: called each time sum(II)I at 5
BSM: called from class Calculator
            from method sum(II)I at line 4
PROBE: called each time sum(II)I at 4
PROBE: called each time sum(II)I at 5
PROBE: called each time sum(II)I at 4
PROBE: called each time sum(II)I at 5
PROBE: called each time sum(II)I at 4
BSM: called from class Calculator
            from method sum(II)I at line 7
PROBE: called each time sum(II)I at 7
sum: 5
```

You can see that the method `bsm` is called once per instruction and the method `probe`
is called as many time the instruction is executed.
You may have notice that the line 4 appears two times in the `LineNumberTable`,
it's because in term of bytecode the for is separated in two parts `for(var x = 0; x < b`
and `; x++)`, the first part, the loop initialisation and the loop test starts at offset 2,
the second part, the loop incrementation is at offset 12.

Also the code the probe is inserted at the beginning of each line is not the right strategy
in case of the for loop, you only see the body of the loop and the loop incrementation being
repeated not the loop test. You need more or less to detect when you come back from a goto,
here there is a trick because those points are the points where there local variables merge
so a StackMap frame should be present.