package com.org.finance.stock;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.TextColor;

public class StockCost {
	ComboBox<String> _stockTranscationMode;
	private ComboBox<String> _feeDiscount;
	private ComboBox<String> _minFee;
	private TextBox _buyPrice;
	private TextBox _sellPrice;

	private Label _costBuyFee;
	private Label _costSellFee;
	private Label _costTax;
	private Label _cost;
	private Label _result1;
	private Label _result2;

	private int getFee(float stockBuyPrice, float feeDiscountPercentage) {
		int fee = (int)(stockBuyPrice * 1000.0f * 0.001425f * feeDiscountPercentage);
		return fee;
	}

	private int calculateResult(float stockSellPrice, float stockBuyPrice, float stockTaxPercetage, float feeDiscountPercentage, int lowFee) {
		int fee1 = (int)(stockBuyPrice * 1000.0f * 0.001425f * feeDiscountPercentage);
		int fee2 = (int)(stockSellPrice * 1000.0f * 0.001425f * feeDiscountPercentage);

		if (fee1 < lowFee)
			fee1 = lowFee;

		if (fee2 < lowFee)
			fee2 = lowFee;

		int tax = (int)(stockSellPrice * 1000.0f * stockTaxPercetage);
		int diff = (int) (stockSellPrice * 1000.0f - stockBuyPrice * 1000.0f);
		int r = diff - (fee1 + fee2 + tax);

		return r;
	}

	public void updateCost() {
		try {
			float feeDiscountPercentage = 1.0f;

			switch (_feeDiscount.getSelectedIndex()) {
				case 1:
					feeDiscountPercentage = 65.0f / 100.0f;
					break;

				case 2:
					feeDiscountPercentage = 60.0f / 100.0f;
					break;

				case 3:
					feeDiscountPercentage = 50.0f / 100.0f;
					break;

				case 4:
					feeDiscountPercentage = 30.0f / 100.0f;
					break;

				case 5:
					feeDiscountPercentage = 28.0f / 100.0f;
					break;
			}

			int lowFee = 20;
			if (_minFee.getSelectedIndex() == 1)
				lowFee = 8;

			float stockBuyPrice = Float.valueOf(_buyPrice.getText());
			float stockSellPrice = Float.valueOf(_sellPrice.getText());

			int fee1 = getFee(stockBuyPrice, feeDiscountPercentage);
			int fee2 = getFee(stockSellPrice, feeDiscountPercentage);

			float stockTaxPercetage = 0.0015f;
			if (_stockTranscationMode.getSelectedIndex() == 1)
				stockTaxPercetage = 0.003f;

			int tax = (int)(stockSellPrice * 1000.0f * stockTaxPercetage);
			int diff = (int) (stockSellPrice * 1000.0f - stockBuyPrice * 1000.0f);

			int r1 = calculateResult(stockSellPrice, stockBuyPrice, stockTaxPercetage, feeDiscountPercentage, lowFee);
			int r2 = calculateResult(stockSellPrice, stockBuyPrice, stockTaxPercetage, 1.0f, lowFee);

			_costBuyFee.setText("買入手續費: " + fee1);
			_costSellFee.setText("賣出手續費: " + fee2);
			_costTax.setText("    証交稅: " + tax);
			_cost.setText("    總成本: " + (fee1 + fee2 + tax));

			if (r1 > 0)
				_result1.setForegroundColor(TextColor.ANSI.RED);
			else if (r1 < 0)
				_result1.setForegroundColor(TextColor.ANSI.GREEN);
			else if (r1 == 0)
				_result1.setForegroundColor(TextColor.ANSI.DEFAULT);

			_result1.setText("折扣後收益: " + r1);

			if (r2 > 0)
				_result2.setForegroundColor(TextColor.ANSI.RED);
			else if (r2 < 0)
				_result2.setForegroundColor(TextColor.ANSI.GREEN);
			else if (r2 == 0)
				_result2.setForegroundColor(TextColor.ANSI.DEFAULT);

			_result2.setText("  帳面收益: " + r2);
		} catch (Exception e) {
		}
	}

	public void handle() {
		try {
			final Screen screen = new DefaultTerminalFactory().createScreen();
			screen.startScreen();

			final Window window = new BasicWindow("股票成本計算器");
			final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

			Panel contentPanel = new Panel(new GridLayout(2));
			GridLayout gridLayout = (GridLayout)contentPanel.getLayoutManager();
			gridLayout.setHorizontalSpacing(3);

			contentPanel.addComponent(new Label("交易方式:"));
			_stockTranscationMode = new ComboBox<String>("當沖", "現股");
			_stockTranscationMode.setReadOnly(true);
			_stockTranscationMode.setPreferredSize(new TerminalSize(10, 1));

			final Label tax = new Label("証交稅: 0.15%");

			_stockTranscationMode.addListener(new ComboBox.Listener() {
				public void onSelectionChanged(int selectedIndex, int previousSelection) {
					if (selectedIndex == 0) 
						tax.setText("証交稅: 0.15%");
					else
						tax.setText("証交稅: 0.3%");

					updateCost();
				}
			});

			contentPanel.addComponent(_stockTranscationMode);

			contentPanel.addComponent(new Label("手續費折扣:"));
			_feeDiscount = new ComboBox<String>("無折扣", "65 折", "6 折", "5 折", "3 折", "28 折");
			_feeDiscount.setSelectedIndex(1);
			_feeDiscount.setReadOnly(true);
			_feeDiscount.setPreferredSize(new TerminalSize(10, 1));

			_feeDiscount.addListener(new ComboBox.Listener() {
				public void onSelectionChanged(int selectedIndex, int previousSelection) {
					updateCost();
				}
			});

			contentPanel.addComponent(_feeDiscount);

			contentPanel.addComponent(new Label("最低手續費:"));
			_minFee = new ComboBox<String>("20 塊", "8 塊");
			_minFee.setReadOnly(true);
			_minFee.setPreferredSize(new TerminalSize(10, 1));

			_minFee.addListener(new ComboBox.Listener() {
				public void onSelectionChanged(int selectedIndex, int previousSelection) {
					updateCost();
				}
			});

			contentPanel.addComponent(_minFee);
			contentPanel.addComponent(tax);

			tax.setLayoutData(GridLayout.createLayoutData(
						GridLayout.Alignment.BEGINNING,
						GridLayout.Alignment.BEGINNING,
						true, false, 2, 1));

			contentPanel.addComponent(new Separator(Direction.HORIZONTAL)
					.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));

			contentPanel.addComponent(new Label("買入價:"));
			_buyPrice = new TextBox();

			_buyPrice = new TextBox() {
				protected void afterLeaveFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
					updateCost();
				}
			};

			_buyPrice.addTo(contentPanel);

			contentPanel.addComponent(new Separator(Direction.HORIZONTAL)
					.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));

			contentPanel.addComponent(new Label("賣出價:"));

			_costBuyFee = new Label("買入手續費: ----");
			_costSellFee = new Label("賣出手續費: ----");
			_costTax = new Label("    証交稅: ----");
			_cost = new Label("    總成本: ----");
			_result1 = new Label("折扣後收益: ----");
			_result2 = new Label("  帳面收益: ----");

			_sellPrice = new TextBox() {
				protected void afterLeaveFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
					updateCost();
				}
			};

			_sellPrice.addTo(contentPanel);

			contentPanel.addComponent(new Separator(Direction.HORIZONTAL)
					.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));

			contentPanel.addComponent(_costBuyFee);
			contentPanel.addComponent(_costSellFee);
			contentPanel.addComponent(_costTax);
			contentPanel.addComponent(_cost);
			contentPanel.addComponent(_result1);
			contentPanel.addComponent(_result2);

			window.setComponent(contentPanel);

			_buyPrice.takeFocus();

			textGUI.addWindowAndWait(window);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void main(String [] args) {
		StockCost stockCost = new StockCost();
		stockCost.handle();
	}
}
