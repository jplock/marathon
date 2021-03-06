package mesosphere.marathon

class Exception(msg: String) extends scala.RuntimeException(msg)

class StorageException(msg: String) extends Exception(msg)

class UnknownAppException(id: String) extends Exception(s"App '$id' does not exist")

class BadRequestException(msg: String) extends Exception(msg)
