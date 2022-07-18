package com.funnyboyroks;

public class Main {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: WordPainter.jar [1] <...args>");
            System.exit(1);
        }

        if (args[0].equals("1")) {
            String[] args2 = new String[args.length - 1];
            System.arraycopy(args, 1, args2, 0, args2.length);
            DrawOne.run(args2);
        } else {
            DrawMany.run(args);
        }

    }

}
