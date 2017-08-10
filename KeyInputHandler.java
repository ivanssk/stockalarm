import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.input.KeyStroke;

public class KeyInputHandler {
	public interface IListener {
		void onStartTextInputModeEnter(Screen screen, TextGraphics textGraphics, boolean buyMode, boolean sellMode) throws Exception;
		void onLeaveTextInputModeEnter(Screen screen, TextGraphics textGraphics, boolean isFinished) throws Exception;
		void onCharacter(Screen screen, TextGraphics textGraphics, Character c) throws Exception;
		void onBackspace(Screen screen, TextGraphics textGraphics) throws Exception;
	}

	private boolean _text_input_mode;

	public boolean handle(Screen screen, TextGraphics textGraphics, IListener listener) throws Exception {
		KeyStroke k = screen.pollInput();

		if (k == null)
			return true;

		KeyType keyType = k.getKeyType();

		if (keyType != null && _text_input_mode == true) {
			if (keyType == KeyType.Escape) {
				listener.onLeaveTextInputModeEnter(screen, textGraphics, false);
				_text_input_mode = false;
				return true;
			} else if (keyType == KeyType.Enter) {
				listener.onLeaveTextInputModeEnter(screen, textGraphics, true);
				_text_input_mode = false;
				return true;
			} else if (keyType == KeyType.Backspace) {
				listener.onBackspace(screen, textGraphics);
				return true;
			} else if (_text_input_mode == true) {
				Character c = k.getCharacter();
				listener.onCharacter(screen, textGraphics, c);
				return true;
			}
		}

		if (_text_input_mode == false) {
			Character c = k.getCharacter();
			if (c == null)
				return true;

			if (c.equals('q') == true) {
				return false;
			} else if (c.equals('s') == true) {
				_text_input_mode = true;
				listener.onStartTextInputModeEnter(screen, textGraphics, false, true);
			} else if (c.equals('b') == true) {
				_text_input_mode = true;
				listener.onStartTextInputModeEnter(screen, textGraphics, true, false);
			}
		}

		return true;
	}
}
