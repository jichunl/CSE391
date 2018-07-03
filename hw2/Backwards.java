public class Backwards{
	public static void main(String[] args) {
		for (String arg : args) {
			for (int i = arg.length() - 1; i >= 0; i--) {
				System.out.print(arg.charAt(i));
			}
			System.out.println();
		}
	}
}
