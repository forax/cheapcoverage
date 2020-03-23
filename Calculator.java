public class Calculator {
  public static int sum(int a, int b) {
    var sum = a;
    for(var x = 0; x < b; x++) {
      sum++;
    }
    return sum;
  }
}