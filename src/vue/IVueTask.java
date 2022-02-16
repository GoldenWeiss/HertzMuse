package vue;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import model.audio.ThreadModele;
import model.utility.Vector;


public abstract class IVueTask {
	
	static final double FPS = ThreadModele.FPS;
	boolean close;
	Stage wndptr;
	
	abstract void draw();
	abstract void update();
	
	IVueTask() {
		close = false;
		wndptr = new Stage();
		wndptr.setOnCloseRequest(e -> close = true);
		
	}
	
	boolean closed() {
		return close;
	}
	void close() {
		close = true;
		wndptr.close();
	}
	void requestFocus() {
		wndptr.requestFocus();
	}
	
	Stage getStage() {
		return wndptr;
	}
	
	@Deprecated
	/**
	 * Use in mother constructor an AnimationTimer loop for Platform.runLater(()->draw())
	 * & add in mother update call : Platform.runLater(()->update())
	 * 
	 */
	void start() {
		Thread t = new Thread(getTask());
		t.setDaemon(true);
		t.start();
	}
	
	@Deprecated
	private Task<Void> getTask() {
		return new Task<Void>() {
			private long st;
			
			@Override
			public Void call() throws Exception {
				{
					st = System.currentTimeMillis();
					while (!close) {
						update();
						Platform.runLater(() -> draw());
						Thread.sleep((long) (1_000 / FPS) + System.currentTimeMillis() - st);
						st = System.currentTimeMillis();
					}
				}
				return null;
			}
		};

	}

}
