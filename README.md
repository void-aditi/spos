# spos

🧮 Algorithm: JNI Arithmetic Operations using Dynamic Link Library


---

Step 1: Start

Begin the program execution.


---

Step 2: Create Java Source File

1. Write a Java program ArithmeticDemo.java.


2. Declare native methods for:

add(int a, int b)

subtract(int a, int b)

multiply(int a, int b)

divide(int a, int b)



3. Use System.loadLibrary("arithmeticDemo") to load the DLL file.


4. In the main() method, take two numbers as input and call the native methods.




---

Step 3: Compile Java File

Run:

javac ArithmeticDemo.java

This generates the ArithmeticDemo.class file (bytecode).


---

Step 4: Generate Header File

Run:

javac -h . ArithmeticDemo.java

This creates the JNI header file ArithmeticDemo.h that defines the link between Java and C.


---

Step 5: Write the C Program

1. Create a C file named ArithmeticDemo.c.


2. Include:

#include <jni.h>
#include "ArithmeticDemo.h"


3. Implement all four arithmetic functions:

Java_ArithmeticDemo_add

Java_ArithmeticDemo_subtract

Java_ArithmeticDemo_multiply

Java_ArithmeticDemo_divide





---

Step 6: Compile C Code to DLL

Use GCC to create a dynamic link library:

For Windows:

gcc -shared -o arithmeticDemo.dll -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" ArithmeticDemo.c

For Linux:

gcc -shared -fPIC -o libarithmeticDemo.so -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" ArithmeticDemo.c

This produces the dynamic library (.dll or .so).


---

Step 7: Execute the Java Program

Run:

java ArithmeticDemo

When executed:

1. Java loads the DLL dynamically using System.loadLibrary().


2. The native functions are called through JNI.


3. The C functions perform arithmetic operations.


4. Results are returned to Java and displayed.

Step 8: Stop

End of program.
