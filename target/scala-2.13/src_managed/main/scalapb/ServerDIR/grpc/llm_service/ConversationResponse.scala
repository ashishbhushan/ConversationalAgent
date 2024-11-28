// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package ServerDIR.grpc.llm_service

@SerialVersionUID(0L)
final case class ConversationResponse(
    inputTextTokenCount: _root_.scala.Int = 0,
    results: _root_.scala.Seq[ServerDIR.grpc.llm_service.CompletionResult] = _root_.scala.Seq.empty,
    unknownFields: _root_.scalapb.UnknownFieldSet = _root_.scalapb.UnknownFieldSet.empty
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[ConversationResponse] {
    @transient
    private[this] var __serializedSizeMemoized: _root_.scala.Int = 0
    private[this] def __computeSerializedSize(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = inputTextTokenCount
        if (__value != 0) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(1, __value)
        }
      };
      results.foreach { __item =>
        val __value = __item
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      }
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
        val __v = inputTextTokenCount
        if (__v != 0) {
          _output__.writeInt32(1, __v)
        }
      };
      results.foreach { __v =>
        val __m = __v
        _output__.writeTag(2, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      unknownFields.writeTo(_output__)
    }
    def withInputTextTokenCount(__v: _root_.scala.Int): ConversationResponse = copy(inputTextTokenCount = __v)
    def clearResults = copy(results = _root_.scala.Seq.empty)
    def addResults(__vs: ServerDIR.grpc.llm_service.CompletionResult *): ConversationResponse = addAllResults(__vs)
    def addAllResults(__vs: Iterable[ServerDIR.grpc.llm_service.CompletionResult]): ConversationResponse = copy(results = results ++ __vs)
    def withResults(__v: _root_.scala.Seq[ServerDIR.grpc.llm_service.CompletionResult]): ConversationResponse = copy(results = __v)
    def withUnknownFields(__v: _root_.scalapb.UnknownFieldSet) = copy(unknownFields = __v)
    def discardUnknownFields = copy(unknownFields = _root_.scalapb.UnknownFieldSet.empty)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = inputTextTokenCount
          if (__t != 0) __t else null
        }
        case 2 => results
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PInt(inputTextTokenCount)
        case 2 => _root_.scalapb.descriptors.PRepeated(results.iterator.map(_.toPMessage).toVector)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion: ServerDIR.grpc.llm_service.ConversationResponse.type = ServerDIR.grpc.llm_service.ConversationResponse
    // @@protoc_insertion_point(GeneratedMessage[ServerDIR.ConversationResponse])
}

object ConversationResponse extends scalapb.GeneratedMessageCompanion[ServerDIR.grpc.llm_service.ConversationResponse] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[ServerDIR.grpc.llm_service.ConversationResponse] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): ServerDIR.grpc.llm_service.ConversationResponse = {
    var __inputTextTokenCount: _root_.scala.Int = 0
    val __results: _root_.scala.collection.immutable.VectorBuilder[ServerDIR.grpc.llm_service.CompletionResult] = new _root_.scala.collection.immutable.VectorBuilder[ServerDIR.grpc.llm_service.CompletionResult]
    var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __inputTextTokenCount = _input__.readInt32()
        case 18 =>
          __results += _root_.scalapb.LiteParser.readMessage[ServerDIR.grpc.llm_service.CompletionResult](_input__)
        case tag =>
          if (_unknownFields__ == null) {
            _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
          }
          _unknownFields__.parseField(tag, _input__)
      }
    }
    ServerDIR.grpc.llm_service.ConversationResponse(
        inputTextTokenCount = __inputTextTokenCount,
        results = __results.result(),
        unknownFields = if (_unknownFields__ == null) _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[ServerDIR.grpc.llm_service.ConversationResponse] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      ServerDIR.grpc.llm_service.ConversationResponse(
        inputTextTokenCount = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Int]).getOrElse(0),
        results = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Seq[ServerDIR.grpc.llm_service.CompletionResult]]).getOrElse(_root_.scala.Seq.empty)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = LlmServiceProto.javaDescriptor.getMessageTypes().get(1)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = LlmServiceProto.scalaDescriptor.messages(1)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 2 => __out = ServerDIR.grpc.llm_service.CompletionResult
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = ServerDIR.grpc.llm_service.ConversationResponse(
    inputTextTokenCount = 0,
    results = _root_.scala.Seq.empty
  )
  implicit class ConversationResponseLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, ServerDIR.grpc.llm_service.ConversationResponse]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, ServerDIR.grpc.llm_service.ConversationResponse](_l) {
    def inputTextTokenCount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Int] = field(_.inputTextTokenCount)((c_, f_) => c_.copy(inputTextTokenCount = f_))
    def results: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Seq[ServerDIR.grpc.llm_service.CompletionResult]] = field(_.results)((c_, f_) => c_.copy(results = f_))
  }
  final val INPUT_TEXT_TOKEN_COUNT_FIELD_NUMBER = 1
  final val RESULTS_FIELD_NUMBER = 2
  def of(
    inputTextTokenCount: _root_.scala.Int,
    results: _root_.scala.Seq[ServerDIR.grpc.llm_service.CompletionResult]
  ): _root_.ServerDIR.grpc.llm_service.ConversationResponse = _root_.ServerDIR.grpc.llm_service.ConversationResponse(
    inputTextTokenCount,
    results
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[ServerDIR.ConversationResponse])
}