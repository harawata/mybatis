package org.apache.ibatis.executor.result;

public class DefaultResultContext implements ResultContext {

private Object resultObject;
  private int resultCount;
  private boolean stopped;

  public DefaultResultContext() {
    resultObject = null;
    resultCount = 0;
    stopped = false;
  }

  public Object getResultObject() {
    return resultObject;
  }

  public int getResultCount() {
    return resultCount;
  }

  public boolean isStopped() {
    return stopped;
  }

  public void nextResultObject(Object resultObject) {
    resultCount++;
    this.resultObject = resultObject;
  }

  public void stop() {
    this.stopped = true;
  }

}
