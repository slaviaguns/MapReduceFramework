package mapreduce;
import java.io.File;
import java.util.*;

public class JobMeta {

  public enum JobStatus {
    INIT, INPROGRESS, FAILED, SUCCEED
  }

  // the input blocks after splitting the input data
  public class InputBlock {
    private String filePath;

    private long offset;

    private int length;

    public InputBlock(String fp, long o, int l) {
      this.filePath = fp;
      this.offset = o;
      this.length = l;
    }

    public String getFilePath() {
      return filePath;
    }

    public long getOffset() {
      return offset;
    }

    public int getLength() {
      return length;
    }
  }

  // the id for the job
  private int jobId;

  // the name for each map/reduce job
  private String jobName;

  private String mapperClassName;

  private String reducerClassName;
  
  private String partitionerClassName;
  
  private String inputFormatClassName;
  
  private String outputFormatClassName;

  private String inputPath;

  private String outputPath;

  private int blockSize;

  private int reducerNum;

  private Set<Integer> mapTasks;

  private Set<Integer> reduceTasks;
  
  private int taskFinishedMapperNum;
  
  private int taskFinishedReducerNum;

  private List<InputBlock> inputBlocks;
  
  private JobStatus status;

  public JobMeta(JobConf jconf) {
    this.jobId = jconf.getJobID();
    this.jobName = jconf.getJobName();
//    this.mapperClassName = JobTracker.JOB_CLASSPATH_PREFIX + this.jobId + "."
//            + jconf.getMapperClassName();
//    this.reducerClassName = JobTracker.JOB_CLASSPATH_PREFIX + this.jobId + "."
//            + jconf.getReducerClassName();
//    this.partitionerClassName = JobTracker.JOB_CLASSPATH_PREFIX + this.jobId + "."
//            + jconf.getPartitionerClassName();
//    this.inputFormatClassName = JobTracker.JOB_CLASSPATH_PREFIX + this.jobId + "."
//            + jconf.getInputFormatClassName();
//    this.outputFormatClassName = JobTracker.JOB_CLASSPATH_PREFIX + this.jobId + "."
//            + jconf.getOutputFormatClassName();
    this.mapperClassName = jconf.getMapperClassName();
    this.reducerClassName = jconf.getReducerClassName();
    this.partitionerClassName = jconf.getPartitionerClassName();
    this.inputFormatClassName = jconf.getInputFormatClassName();
    this.outputFormatClassName = jconf.getOutputFormatClassName();
    this.inputPath = jconf.getInputPath();
    this.outputPath = jconf.getOutputPath();
    this.blockSize = jconf.getBlockSize();
    this.reducerNum = jconf.getReducerNum();
    this.taskFinishedMapperNum = 0;
    this.taskFinishedReducerNum = 0;
    this.status = JobStatus.INIT;

    this.mapTasks = new HashSet<Integer>();
    this.reduceTasks = new HashSet<Integer>();
    this.inputBlocks = new ArrayList<InputBlock>();
  }

  /**
   * split all the input files
   */
  public void splitInput() {
    if (this.inputBlocks == null)
      this.inputBlocks = new ArrayList<InputBlock>();
    else
      this.inputBlocks.clear();

    File f = new File(this.inputPath);
    if (f.isDirectory()) {
      // here, we assume that there could only be one level directory for
      // the input directory
      File[] files = f.listFiles();
      for (File file : files)
        this.inputBlocks.addAll(this.splitOneInputFile(file));
    } else {
      this.inputBlocks.addAll(this.splitOneInputFile(f));
    }
  }

  /**
   * split a single file
   * 
   * @param file
   * @return
   */
  private List<InputBlock> splitOneInputFile(File file) {
    List<InputBlock> result = new ArrayList<InputBlock>();

    long len = file.length();
    long offset = 0;
    while (len > 0) {
      if (len > (this.blockSize + this.blockSize / 2)) {
        // if the rest content is 1.5 times long than the blocksize, just split
        // a normal block
        result.add(new InputBlock(file.getAbsolutePath(), offset, this.blockSize));
        len -= this.blockSize;
        offset += this.blockSize;
      } else {
        // otherwise, take all the rest content as a block
        result.add(new InputBlock(file.getAbsolutePath(), offset, (int) len));
        len = 0;
      }
    }

    return result;
  }
  
  public boolean isDone() {
    return ((this.taskFinishedMapperNum + this.taskFinishedReducerNum) == (this.mapTasks.size() + this.reduceTasks.size()));
  }
  
  public void reportFinishedTask(int tid) {
    if (this.mapTasks.contains(tid)) {
      this.taskFinishedMapperNum ++;
    }
    
    if (this.reduceTasks.contains(tid)) {
      this.taskFinishedReducerNum ++;
    }
  }
  
  public int getFinishedMapperNumber() {
    return this.taskFinishedMapperNum;
  }
  
  public int getFinishedReducerNumber() {
    return this.taskFinishedReducerNum;
  }

  public int getJobId() {
    return jobId;
  }

  public void setJobId(int jobId) {
    this.jobId = jobId;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getMapperClassName() {
    return mapperClassName;
  }

  public void setMapperClassName(String mapperClassName) {
    this.mapperClassName = mapperClassName;
  }

  public String getReducerClassName() {
    return reducerClassName;
  }

  public void setReducerClassName(String reducerClassName) {
    this.reducerClassName = reducerClassName;
  }

  public String getInputPath() {
    return inputPath;
  }

  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

  public Set<Integer> getMapTasks() {
    return mapTasks;
  }

  public Set<Integer> getReduceTasks() {
    return reduceTasks;
  }

  public List<InputBlock> getInputBlocks() {
    return Collections.unmodifiableList(inputBlocks);
  }

  public int getReducerNum() {
    return reducerNum;
  }

  public void addMapperTask(int taskid) {
    this.mapTasks.add(taskid);
  }

  public void addReducerTask(int taskid) {
    this.reduceTasks.add(taskid);
  }
  
  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public String getPartitionerClassName() {
    return partitionerClassName;
  }

  public void setPartitionerClassName(String partitionerClassName) {
    this.partitionerClassName = partitionerClassName;
  }
  
  public String getInputFormatClassName() {
    return inputFormatClassName;
  }

  public void setInputFormatClassName(String inputFormatClassName) {
    this.inputFormatClassName = inputFormatClassName;
  }
  
  public String getOutputFormatClassName() {
    return outputFormatClassName;
  }

  public void setOutputFormatClassName(String outputFormatClassName) {
    this.outputFormatClassName = outputFormatClassName;
  }
}