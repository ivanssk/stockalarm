import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.TerminalPosition;

public class KeyFunctionHandler implements Keyboard.IListener {
	private String _text_input;
	private boolean _buy_mode;
	private boolean _sell_mode;
	private Float _my_price;

	public Float getMyPrice() {
		return _my_price;
	}

	public boolean isBuyMode() {
		return _buy_mode;
	}

	public boolean isSellMode() {
		return _sell_mode;
	}

	public void onStartTextInputModeEnter(Screen screen, TextGraphics textGraphics, boolean buyMode, boolean sellMode) throws Exception {
		_text_input = new String();

		if (buyMode == true)
			textGraphics.putString(0, 21, "當沖買入: ");
		else
			textGraphics.putString(0, 21, "當沖賣出: ");

		screen.setCursorPosition(new TerminalPosition(10, 21));
		screen.refresh();

		_buy_mode = buyMode;
		_sell_mode = sellMode;
	}

	public void onLeaveTextInputModeEnter(Screen screen, TextGraphics textGraphics, boolean isFinished) throws Exception {
		if (isFinished == true)
			_my_price = Float.valueOf(_text_input);
		else {
			_my_price = null;
			_text_input = null;
		}

		screen.setCursorPosition(new TerminalPosition(0, 0));
		textGraphics.putString(0, 21, "                    ");
		screen.refresh();
	}

	public void onCharacter(Screen screen, TextGraphics textGraphics, Character c) throws Exception {
		if (c != '.' && Character.isDigit(c) == false)
			return;

		_text_input += c;
		screen.setCursorPosition(new TerminalPosition(10 + _text_input.length(), 21));

		if (_buy_mode == true)
			textGraphics.putString(0, 21, "當沖買入: " + _text_input);
		else if (_sell_mode == true)
			textGraphics.putString(0, 21, "當沖賣出: " + _text_input);

		screen.refresh();
	}

	public void onBackspace(Screen screen, TextGraphics textGraphics) throws Exception {
		if (_text_input.length() == 0)
			return;

		textGraphics.putString(0, 21, "                    ");

		_text_input = _text_input.substring(0, _text_input.length() - 1);
		screen.setCursorPosition(new TerminalPosition(10 + _text_input.length(), 21));

		if (_buy_mode == true)
			textGraphics.putString(0, 21, "當沖買入: " + _text_input);
		else if (_sell_mode == true)
			textGraphics.putString(0, 21, "當沖賣出: " + _text_input);

		screen.refresh();
	}
}
