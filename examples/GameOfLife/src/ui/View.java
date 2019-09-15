package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame {
	private static final long serialVersionUID = 1L;

	protected IController controller;

	/* JFrame main window and field buttons */
	protected FieldButton[][] buttons;
	
	/**
	 * The view constructor
	 * 
	 * @param controller Specifies the controller object. This must not be null!
	 * @param board      Specifies the board object. This must not be null!
	 */
	public View(IController controller) {
		/* Setup references */
		this.controller = controller;

		/* Initialize all components and update the view for the first time */
		initializeComponents();
		update();
	}

	/**
	 * Initializes all components: creates menu entries, action listener, field
	 * buttons etc.
	 */
	private void initializeComponents() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		/* Compute button size depending on the count of fields and window size */
		Dimension maxBoardSize = new Dimension(1000, 500);

		int width = controller.getWidth();
		int height = controller.getHeight();

		int buttonSize = Math.min(maxBoardSize.width / width, maxBoardSize.height / height);

		/* Create field buttons */
		buttons = new FieldButton[height][width];
		JPanel panelBoard = new JPanel(new GridLayout(height, width));

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				/* Create new field button */
				FieldButton button = new FieldButton();

				/* Initialize button with size and popup menu connection */
				button.setPreferredSize(new Dimension(buttonSize, buttonSize));
				button.setMargin(new Insets(0, 0, 0, 0));
		
				/* Insert button into array */
				buttons[row][col] = button;
				panelBoard.add(button);
			}
		}
		
		for (int row = 0; row < height; row++)
			for(int col = 0; col < width; col++)
				buttons[row][col].setField(controller.getFields()[row][col]);

		/* Finalize main window */
		setTitle("Game of Life");
		add(panelBoard);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				update();
			}
		});
		
		pack();
		setVisible(true);
	}

	public void update() {
		for (int row = 0; row < controller.getHeight(); row++) {
			for (int col = 0; col < controller.getWidth(); col++) {
				/* Get field button at current row and column in array */
				FieldButton button = buttons[row][col];

				/* Get field from field-button */
				Field f = button.getField();

				/* Get figure icon */
				button.setText("");
				
				/* Setup border and background color */
				if (f.isAlive()) {
					button.setBackground(Color.RED);
					button.setBorder(BorderFactory.createLineBorder(Color.RED));
				} else {
					button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
					button.setBackground(null);
				}
			}
		}

		this.repaint();
	}

	/**
	 * @return The controller object.
	 */
	public IController getController() {
		return controller;
	}
}
