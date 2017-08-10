//import com.googlecode.lanterna.SGR;
//import com.googlecode.lanterna.TerminalPosition;
//import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;

public class Show {
	static public void main(String [] args) {
		//DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

		try {
			Screen screen = new DefaultTerminalFactory().createScreen();

			screen.startScreen();
			TextGraphics textGraphics = screen.newTextGraphics();
			//textGraphics.setForegroundColor(TextColor.ANSI.RED);
			//textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
			textGraphics.putString(0, 0, "Hello Lanterna!");
			screen.refresh();
			Thread.sleep(2000);
			screen.stopScreen();
			/*
			Terminal terminal = defaultTerminalFactory.createTerminal();
			terminal.enterPrivateMode();

			terminal.setForegroundColor(TextColor.ANSI.RED);
			terminal.setBackgroundColor(TextColor.ANSI.BLUE);

			terminal.putCharacter('H');
			terminal.putCharacter('e');
			terminal.putCharacter('l');
			terminal.putCharacter('l');
			terminal.putCharacter('o');
			terminal.putCharacter('\n');

			terminal.setForegroundColor(TextColor.ANSI.DEFAULT);
			terminal.setBackgroundColor(TextColor.ANSI.DEFAULT);

			terminal.flush();

			Thread.sleep(1000);

			terminal.setCursorPosition(0, 0);
			terminal.putCharacter('a');
			terminal.flush();
			Thread.sleep(2000);

			terminal.exitPrivateMode();
			*/
		} catch (Exception e) {
		}
	}
}
