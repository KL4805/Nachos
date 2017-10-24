package nachos.threads;

import java.util.Vector;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		long time_now=Machine.timer().getTime();
		for (int i=0;i<wakeup_queue.size();i++)
		{
			if (time_now>wakeup_queue.elementAt(i).wakeup_time)
			{
				Machine.interrupt().disable();
				wakeup_queue.elementAt(i).kThread.ready();
				Machine.interrupt().enable();
				wakeup_queue.remove(i);
				i--;
			}
		}
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		if (x>0)
		{
			long wakeTime = Machine.timer().getTime() + x;
			sleeping_thread sthread = new sleeping_thread(KThread.currentThread(), wakeTime); 
			wakeup_queue.addElement(sthread);
			Machine.interrupt().disable();
			KThread.sleep();
			Machine.interrupt().enable();
			//while (wakeTime > Machine.timer().getTime())
				//KThread.yield();
		}
		
	}
	public static void alarmTest1() {
		int durations[] = {2000, 20*1000, 200*1000};
		long t0, t1;

		for (int d : durations) {
		    t0 = Machine.timer().getTime();
		    ThreadedKernel.alarm.waitUntil (d);
		    t1 = Machine.timer().getTime();
		    System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
	    }
	public static void selfTest() {
		alarmTest1();

		// Invoke your other test methods here ...
	    }
	public class sleeping_thread
	{
		private KThread kThread;
		private long wakeup_time;
		public sleeping_thread(KThread kThread, long wakeup_time)
		{
			this.kThread=kThread;
			this.wakeup_time=wakeup_time;
		}
	}
	private static Vector<sleeping_thread> wakeup_queue = new Vector<sleeping_thread>();
}

