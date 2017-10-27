package nachos.threads;

import java.util.*;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
    private Lock lck;
	private Condition C_speaker;
	private Condition C_listener;
	private int sp_cnt;
	private int ls_cnt;
	private int word_in;
	private int speaking;
	//private Semaphore speaking_speaker;
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		lck=new Lock();
		speaking = 0;
		C_speaker = new Condition(lck);
		C_listener = new Condition(lck);
		//speaking_speaker = new Semaphore(1);
		word_in = 0;
		//sp_cnt = 0;
		ls_cnt = 0;		
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word the integer to transfer.
	 */
	public void speak(int word) {
		lck.acquire();
		//sp_cnt+=1;
		System.out.println("Speaker "+ KThread.currentThread().toString()+ " entered");
		while(ls_cnt == 0 || speaking == 1){
			C_speaker.sleep();
		}
		word_in = word;
		speaking = 1;
		C_listener.wake();
		//speaking_speaker.P();
		//sp_cnt-=1;
		System.out.println("Speaker "+KThread.currentThread().toString()+" leaves");
		lck.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		lck.acquire();
		System.out.println("Listener "+KThread.currentThread().toString()+" entered");
		ls_cnt +=1;
		while(speaking == 0){
			C_speaker.wake();
			C_listener.sleep();
		}
		int wd = word_in;
		speaking = 0;
		ls_cnt -= 1;
		//speaking_speaker.V();
		C_speaker.wake();
		lck.release();
		System.out.println("Listener "+KThread.currentThread().toString()+" leaves");

		return wd;
	}

    public static void commTest6() {
	final Communicator com = new Communicator();
	final long times[] = new long[4];
	final int words[] = new int[2];
	KThread speaker1 = new KThread( new Runnable () {
		public void run() {
		    com.speak(4);
		    times[0] = Machine.timer().getTime();
		}
	    });
	speaker1.setName("S1");
	KThread speaker2 = new KThread( new Runnable () {
		public void run() {
		    com.speak(7);
		    times[1] = Machine.timer().getTime();
		}
	    });
	speaker2.setName("S2");
	KThread listener1 = new KThread( new Runnable () {
		public void run() {
		    times[2] = Machine.timer().getTime();
		    words[0] = com.listen();
		}
	    });
	listener1.setName("L1");
	KThread listener2 = new KThread( new Runnable () {
		public void run() {
		    times[3] = Machine.timer().getTime();
		    words[1] = com.listen();
		}
	    });
	listener2.setName("L2");
    
	speaker1.fork(); speaker2.fork(); listener1.fork(); listener2.fork();
	speaker1.join(); speaker2.join(); listener1.join(); listener2.join();
    

	Lib.assertTrue(words[0] == 4, "Didn't listen back spoken word."); 
	Lib.assertTrue(words[1] == 7, "Didn't listen back spoken word.");
	Lib.assertTrue(times[0] > times[2], "speak() returned before listen() called.");
	Lib.assertTrue(times[1] > times[3], "speak() returned before listen() called.");
	System.out.println("commTest6 successful!");
    }

    // Invoke Communicator.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
	// place calls to simpler Communicator tests that you implement here

	commTest6();
    }
}
