import 'package:flutter/material.dart';
import 'screens/chat_screen.dart';
import 'constants/theme.dart';

void main() {
  runApp(const DrTayousApp());
}

class DrTayousApp extends StatelessWidget {
  const DrTayousApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'مرشد الدكتور زيد طاووس',
      theme: appTheme,
      home: const ChatScreen(),
      debugShowCheckedModeBanner: false,
    );
  }
}
