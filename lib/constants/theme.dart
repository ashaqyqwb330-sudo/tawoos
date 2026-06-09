import 'package:flutter/material.dart';

final ThemeData appTheme = ThemeData(
  primaryColor: const Color(0xFF0D1B2A), // Navy
  scaffoldBackgroundColor: const Color(0xFF0D1B2A),
  colorScheme: ColorScheme.fromSeed(
    seedColor: const Color(0xFF0D1B2A),
    primary: const Color(0xFF0D1B2A),
    secondary: const Color(0xFFC5A44E), // Gold
    background: const Color(0xFF0D1B2A),
  ),
  appBarTheme: const AppBarTheme(
    backgroundColor: Color(0xFF0D1B2A),
    foregroundColor: Color(0xFFC5A44E),
  ),
);
