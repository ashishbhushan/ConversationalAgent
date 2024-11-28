// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package ServerDIR.grpc.llm_service

@SerialVersionUID(0L)
final case class CompletionResult(
    tokenCount: _root_.scala.Int = 0,
    outputText: _root_.scala.Predef.String = "",
    completionReason: _root_.scala.Predef.String = "",
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[CompletionResult] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = tokenCount
        if (__value != 0) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(1, __value)
        }
      };
      
      {
        val __value = outputText
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
        }
      };
      
      {
        val __value = completionReason
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(3, __value)
        }
      };
      __size += unknownFields.serializedSize
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var __size = __serializedSizeMemoized
      if (__size == 0) {
        __size = __computeSerializedSize() + 1
        __serializedSizeMemoized = __size
      }
      __size - 1
      
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = tokenCount
        if (__v != 0) {
          _output__.writeInt32(1, __v)
        }
      };
      {
        val __v = outputText
        if (!__v.isEmpty) {
          _output__.writeString(2, __v)
        }
      };
      {
        val __v = completionReason
        if (!__v.isEmpty) {
          _output__.writeString(3, __v)
        }
      };
      unknownFields.writeTo(_output__)
    }
    def withTokenCount(__v: _root_.scala.Int): CompletionResult = copy(tokenCount = __v)
    def withOutputText(__v: _root_.scala.Predef.String): CompletionResult = copy(outputText = __v)
    def withCompletionReason(__v: _root_.scala.Predef.String): CompletionResult = copy(completionReason = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = tokenCount
          if (__t != 0) __t else null
        }
        case 2 => {
          val __t = outputText
          if (__t != "") __t else null
        }
        case 3 => {
          val __t = completionReason
          if (__t != "") __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PInt(tokenCount)
        case 2 => _root_.scalapb.descriptors.PString(outputText)
        case 3 => _root_.scalapb.descriptors.PString(completionReason)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion: ServerDIR.grpc.llm_service.CompletionResult.type = ServerDIR.grpc.llm_service.CompletionResult
    // @@protoc_insertion_point(GeneratedMessage[ServerDIR.CompletionResult])
}

object CompletionResult extends scalapb.GeneratedMessageCompanion[ServerDIR.grpc.llm_service.CompletionResult] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[ServerDIR.grpc.llm_service.CompletionResult] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): ServerDIR.grpc.llm_service.CompletionResult = {
    var __tokenCount: _root_.scala.Int = 0
    var __outputText: _root_.scala.Predef.String = ""
    var __completionReason: _root_.scala.Predef.String = ""
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __tokenCount = _input__.readInt32()
        case 18 =>
          __outputText = _input__.readStringRequireUtf8()
        case 26 =>
          __completionReason = _input__.readStringRequireUtf8()
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    ServerDIR.grpc.llm_service.CompletionResult(
        tokenCount = __tokenCount,
        outputText = __outputText,
        completionReason = __completionReason,
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[ServerDIR.grpc.llm_service.CompletionResult] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      ServerDIR.grpc.llm_service.CompletionResult(
        tokenCount = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Int]).getOrElse(0),
        outputText = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        completionReason = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Predef.String]).getOrElse("")
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = LlmServiceProto.javaDescriptor.getMessageTypes().get(2)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = LlmServiceProto.scalaDescriptor.messages(2)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = ServerDIR.grpc.llm_service.CompletionResult(
    tokenCount = 0,
    outputText = "",
    completionReason = ""
  )
  implicit class CompletionResultLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, ServerDIR.grpc.llm_service.CompletionResult]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, ServerDIR.grpc.llm_service.CompletionResult](_l) {
    def tokenCount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Int] = field(_.tokenCount)((c_, f_) => c_.copy(tokenCount = f_))
    def outputText: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.outputText)((c_, f_) => c_.copy(outputText = f_))
    def completionReason: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.completionReason)((c_, f_) => c_.copy(completionReason = f_))
  }
  final val TOKEN_COUNT_FIELD_NUMBER = 1
  final val OUTPUT_TEXT_FIELD_NUMBER = 2
  final val COMPLETION_REASON_FIELD_NUMBER = 3
  def of(
    tokenCount: _root_.scala.Int,
    outputText: _root_.scala.Predef.String,
    completionReason: _root_.scala.Predef.String
  ): _root_.ServerDIR.grpc.llm_service.CompletionResult = _root_.ServerDIR.grpc.llm_service.CompletionResult(
    tokenCount,
    outputText,
    completionReason
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[ServerDIR.CompletionResult])
}