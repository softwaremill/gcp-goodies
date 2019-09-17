package utils

import com.google.cloud.logging.LogEntry
import com.google.cloud.logging.LoggingEnhancer

class StackdriverEnhancer extends LoggingEnhancer {

  override def enhanceLogEntry(logEntry: LogEntry.Builder) = logEntry.addLabel("application", "play-scala-stackdriver-logging")

}