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
	private Queue<Integer> queue;
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		lck=new Lock();
		C_speaker = new Condition(lck);
		C_listener = new Condition(lck);
		queue = new LinkedList<Integer>();
		sp_cnt = 0;
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
		while(ls_cnt ==0){
			C_speaker.sleep();
		}
		sp_cnt+=1;
		queue.add(word);
		sp_cnt-=1;
		lck.release();
		C_listener.wake();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		lck.acquire();
		while(sp_cnt == 0){
			C_listener.sleep();
		}
		ls_cnt +=1;
		int wd = queue.remove();
		ls_cnt -=1;
		C_speaker.wake();
		lck.release();
		return wd;
	}
}
