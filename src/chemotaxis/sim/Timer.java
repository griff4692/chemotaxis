package chemotaxis.sim;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class Timer extends Thread {

	private boolean started = false, completed = false;
	private Callable <?> task = null;
	private Exception error = null;
	private Object result = null;
	private long startTime, endTime;
	private boolean running;

	public Timer() {
		super();
		this.running = true;
	}

	public <T> void callStart(Callable <T> task) {
		if(!isAlive())
			throw new IllegalStateException();
		if(task == null)
			throw new IllegalArgumentException();
		this.task = task;

		synchronized(this) {
			started = true;
			this.startTime = System.currentTimeMillis();
			notify();
		}
	}

	public <T> T callWait(long timeout) throws Exception {
		if(timeout < 0)
			throw new IllegalArgumentException();
		synchronized(this) {
			if(completed == false)
				try {
					wait(timeout);
				} catch(InterruptedException e) {}
		}
		
		if(completed == false)
			throw new TimeoutException();
		completed = false;
		
		if(error != null)
			throw error;
		
		@SuppressWarnings("unchecked")
		T resultT = (T) result;
		
		return resultT;
	}

	public void run() {
		while(true) {
			synchronized(this) {
				if (!this.running) {
					return;
				}
				if(started == false)
					try {
						wait();
					} catch(InterruptedException e) {}
			}
			
			started = false;
			error = null;
			
			try {
				result = task.call();
			} catch(Exception e) {
				error = e;
			}
			
			synchronized(this) {
				this.endTime = System.currentTimeMillis();
				completed = true;
				notify();
			}
		}
	}

	public void terminate() {
		synchronized (this) {
			this.running = false;
		}
	}
	
	public long getElapsedTime() {
		return endTime - startTime;
	}
}