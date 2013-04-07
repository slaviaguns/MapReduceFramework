import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is used for JobClients to communiation with the jobtracker,
 * including get the job id first and then submit a job
 */
public interface JobSubmitter extends Remote {

	public int getJobID() throws RemoteException;
	
	public void submitJob(Object job) throws RemoteException;
	
}
