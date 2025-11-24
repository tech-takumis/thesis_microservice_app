import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/data/services/message_service.dart';
import 'package:mobile/data/models/message.dart';

final messageServiceProvider = Provider<MessageService>((ref) {
  return MessageService.to;
});

// StreamProvider that emits the current messages and listens for updates.
// Fix: Always yield a copy of the messages list to trigger UI updates.
final messagesProvider = StreamProvider.autoDispose<List<Message>>((ref) async* {
  final service = ref.watch(messageServiceProvider);

  print('📡 [messagesProvider] Initializing, current messages count: ${service.messages.length}');

  // Emit the current value immediately if available
  yield [...service.messages];

  // Listen to the stream for updates and always yield a new list instance
  await for (final messages in service.messagesStream) {
    print('📡 [messagesProvider] Received update from stream, count: ${messages.length}');
    yield [...messages];
  }
});
