import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../data/military_first_aid_data.dart';
import '../data/military_first_aid_faq.dart';
import '../services/local_ai_engine.dart';

class MilitaryFirstAidChatScreen extends StatefulWidget {
  const MilitaryFirstAidChatScreen({Key? key}) : super(key: key);

  @override
  State<MilitaryFirstAidChatScreen> createState() => _MilitaryFirstAidChatScreenState();
}

class _MilitaryFirstAidChatScreenState extends State<MilitaryFirstAidChatScreen> {
  final List<Map<String, dynamic>> _messages = [];
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  bool _isTyping = false;

  @override
  void initState() {
    super.initState();
    // Welcome message
    _messages.add({
      'text': 'مرحباً بك في نافذة مستشار دبلوم الإسعاف الحربي الشامل بالمعهد الطبي العسكري. أنا هنا لمساعدتك في فهم اللوائح، الخطة الدراسية، أهداف التدخل الإسعافي MARCH، الحرب الجرثومية، التسكين التكتيكي، والأسئلة المتوقعة الـ 200. كيف يمكنني إرشادك اليوم؟',
      'isUser': false,
      'time': DateTime.now(),
    });
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  // Smart localized responders utilizing Jaccard overlap on the 200 questions of MilitaryFirstAidFaq
  String _processMessageAndGetResponse(String query) {
    return ''; // Deprecated in favor of the enhanced LocalAiEngine-powered _handleSendMessage
  }

  String _normalizeArabic(String text) {
    return LocalAiEngine.normalizeArabic(text);
  }

  void _handleSendMessage({String? customText}) {
    final text = customText ?? _messageController.text.trim();
    if (text.isEmpty) return;

    if (customText == null) {
      _messageController.clear();
    }

    setState(() {
      _messages.add({
        'text': text,
        'isUser': true,
        'time': DateTime.now(),
      });
      _isTyping = true;
    });
    _scrollToBottom();

    // Answer delay emulation
    Future.delayed(const Duration(milliseconds: 600), () {
      if (!mounted) return;
      
      final faqs = MilitaryFirstAidFaq.faq;
      final aiResult = LocalAiEngine.query(text, faqs);

      if (aiResult.answer.isNotEmpty) {
        setState(() {
          _messages.add({
            'text': aiResult.answer,
            'isUser': false,
            'time': DateTime.now(),
            'choices': aiResult.choices,
            'steps': aiResult.steps,
            'currentStepIndex': aiResult.currentStepIndex,
            'isSequential': aiResult.isSequential,
          });
          _isTyping = false;
        });
        _scrollToBottom();
        return;
      }

      // Keyword fallbacks to sections of MilitaryFirstAidData
      String fallbackContent = "";
      final normalizedQuery = LocalAiEngine.normalizeArabic(text);

      for (var section in MilitaryFirstAidData.sections) {
        final title = LocalAiEngine.normalizeArabic(section['title'] ?? '');
        final content = LocalAiEngine.normalizeArabic(section['content'] ?? '');
        if (normalizedQuery.contains(title) || title.contains(normalizedQuery)) {
          fallbackContent = section['content'] ?? '';
          break;
        }
      }

      if (fallbackContent.isEmpty) {
        if (normalizedQuery.contains('شروط') || normalizedQuery.contains('قبول') || normalizedQuery.contains('معدل') || normalizedQuery.contains('ثانويه')) {
          fallbackContent = MilitaryFirstAidData.sections[3]['content'] ?? '';
        } else if (normalizedQuery.contains('تخرج') || normalizedQuery.contains('تتخرج') || normalizedQuery.contains('متطلبات')) {
          fallbackContent = MilitaryFirstAidData.sections[4]['content'] ?? '';
        } else if (normalizedQuery.contains('هدف') || normalizedQuery.contains('اهداف') || normalizedQuery.contains('مخرجات') || normalizedQuery.contains('ilo')) {
          fallbackContent = 'الأهداف الاستراتيجية ومخرجات التعلم لدبلوم الإسعاف الحربي:\n\n' +
              MilitaryFirstAidData.getObjectives().map((o) => '📍 $o').join('\n') +
              '\n\n' +
              MilitaryFirstAidData.getILOs().map((i) => '🔹 $i').join('\n');
        } else if (normalizedQuery.contains('مرفق') || normalizedQuery.contains('مستند') || normalizedQuery.contains('ملف') || normalizedQuery.contains('اداره')) {
          fallbackContent = 'المرفقات والوثائق الإدارية التكتيكية المرتبطة بالبحث العام للدبلوم:\n\n' +
              MilitaryFirstAidData.getFictionalAttachmentData().map((f) => '📁 **${f['title']}** (${f['type']}):\n   ${f['desc']}').join('\n\n');
        }
      }

      if (fallbackContent.isNotEmpty) {
        final parsed = LocalAiEngine.parseSteps(fallbackContent);
        setState(() {
          _messages.add({
            'text': parsed.first,
            'isUser': false,
            'time': DateTime.now(),
            'choices': aiResult.choices,
            'steps': parsed,
            'currentStepIndex': 0,
            'isSequential': parsed.length > 1,
          });
          _isTyping = false;
        });
        _scrollToBottom();
        return;
      }

      // General military fallback context
      final defaultText = 'بناءً على لائحة دبلوم الإسعاف الحربي بالمعهد الطبي العسكري:\n\n' +
          'البرنامج يشتمل على مسار دراسي مكثف وموزع على 36 ساعة معتمدة في فصلين دراسيين، يغطي بروتوكولات الإنعاش من التفجيرات والنزف الشديد و بزل الصدر، ومواجهة الحرب الجرثومية وسوائل الدم بالميدان.\n\n' +
          'هل تود السؤال عن ترياق Mark I لغاز الأعصاب، بروتوكولات فرز START، أو تفاصيل المرفقات الإدارية؟ سأكون سعيداً بإرشادك تكتيكياً بآلية منقذة لحياة الجرحى.';

      setState(() {
        _messages.add({
          'text': defaultText,
          'isUser': false,
          'time': DateTime.now(),
          'choices': aiResult.choices,
          'steps': [],
          'currentStepIndex': 0,
          'isSequential': false,
        });
        _isTyping = false;
      });
      _scrollToBottom();
    });
  }

  void _handleNextStep(List<dynamic> steps, int nextIndex) {
    setState(() {
      _messages.add({
        'text': 'أريد معرفة الخطوة التالية من الإجراء ➡️',
        'isUser': true,
        'time': DateTime.now(),
      });
      _isTyping = true;
    });
    _scrollToBottom();

    Future.delayed(const Duration(milliseconds: 550), () {
      if (!mounted) return;
      setState(() {
        _messages.add({
          'text': steps[nextIndex],
          'isUser': false,
          'time': DateTime.now(),
          'choices': [],
          'steps': steps,
          'currentStepIndex': nextIndex,
          'isSequential': true,
        });
        _isTyping = false;
      });
      _scrollToBottom();
    });
  }

  void _handleAllStepsCombined(List<dynamic> steps, int startIndex) {
    setState(() {
      _messages.add({
        'text': 'أريد استعراض بقية الخطوات بالكامل دفعة واحدة 📃',
        'isUser': true,
        'time': DateTime.now(),
      });
      _isTyping = true;
    });
    _scrollToBottom();

    Future.delayed(const Duration(milliseconds: 650), () {
      if (!mounted) return;
      final remaining = steps.sublist(startIndex).join('\n\n');
      setState(() {
        _messages.add({
          'text': 'إليك بقية الخطوات والإجراءات كاملة:\n\n$remaining',
          'isUser': false,
          'time': DateTime.now(),
          'choices': [],
          'steps': [],
          'currentStepIndex': 0,
          'isSequential': false,
        });
        _isTyping = false;
      });
      _scrollToBottom();
    });
  }

  Future<void> _exportConversation() async {
    if (_messages.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'لا توجد رسائل لتصديرها.',
            style: TextStyle(fontFamily: 'Cairo'),
          ),
          backgroundColor: Colors.redAccent,
        ),
      );
      return;
    }

    try {
      final directory = await getTemporaryDirectory();
      final filePath = '${directory.path}/first_aid_diploma_chat_log.txt';
      final file = File(filePath);

      StringBuffer buffer = StringBuffer();
      buffer.writeln('======================================================');
      buffer.writeln('     سجل محادثة دبلوم الإسعاف الحربي الأكاديمي الشامل');
      buffer.writeln('              إدارة المعهد الطبي العسكري بالقوات المسلحة');
      buffer.writeln('  تحديث: ${DateTime.now().toLocal()}');
      buffer.writeln('======================================================\n');

      for (var msg in _messages) {
        final prefix = msg['isUser'] == true ? '👤 الدارس / المستعلم' : '🤖 المستشار الإسعافي العسكري';
        final timeStr = '${msg['time'].hour}:${msg['time'].minute}';
        buffer.writeln('[$timeStr] $prefix:');
        buffer.writeln('${msg['text']}');
        buffer.writeln('------------------------------------------------------');
      }

      await file.writeAsString(buffer.toString());

      await Share.shareXFiles(
        [XFile(file.path)],
        text: 'مشاركة سجل المستشار العسكري لدبلوم الإسعاف الحربي لإنقاذ أرواح المصابين',
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'خطأ أثناء تصدير المحادثة: $e',
            style: const TextStyle(fontFamily: 'Cairo'),
          ),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  void _showFaqBottomSheet() {
    const Color darkNavy = Color(0xFF0D1B2A);
    const Color solidNavy = Color(0xFF1B263B);
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateGray = Color(0xFFE0E1DD);

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: solidNavy,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(20),
          topRight: Radius.circular(20),
        ),
      ),
      builder: (context) {
        String faqSearch = '';
        return StatefulBuilder(
          builder: (context, setModalState) {
            final List<Map<String, String>> filteredFaqs = MilitaryFirstAidFaq.faq.where((item) {
              final q = _normalizeArabic(item['question'] ?? '');
              final a = _normalizeArabic(item['answer'] ?? '');
              final query = _normalizeArabic(faqSearch);
              return q.contains(query) || a.contains(query);
            }).toList();

            return DraggableScrollableSheet(
              initialChildSize: 0.75,
              minChildSize: 0.5,
              maxChildSize: 0.95,
              expand: false,
              builder: (context, scrollController) {
                return Directionality(
                  textDirection: TextDirection.rtl,
                  child: Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      children: [
                        // Drag handle
                        Container(
                          width: 40,
                          height: 4,
                          decoration: BoxDecoration(
                            color: Colors.white24,
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                        const SizedBox(height: 16),
                        Row(
                          children: [
                            const Icon(Icons.library_books, color: goldenBrass),
                            const SizedBox(width: 8),
                            const Expanded(
                              child: Text(
                                'قاعدة الأسئلة الـ 200 المتوقعة',
                                style: TextStyle(
                                  color: goldenBrass,
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                  fontFamily: 'Cairo',
                                ),
                              ),
                            ),
                            Text(
                              '${filteredFaqs.length} سؤالاً',
                              style: const TextStyle(
                                color: Colors.white60,
                                fontSize: 12,
                                fontFamily: 'Cairo',
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        // Live search bar
                        TextField(
                          autofocus: false,
                          style: const TextStyle(color: Colors.white, fontSize: 13, fontFamily: 'Cairo'),
                          onChanged: (val) {
                            setModalState(() {
                              faqSearch = val;
                            });
                          },
                          decoration: InputDecoration(
                            hintText: 'ابحث في الـ 200 سؤال والبروتوكولات...',
                            hintStyle: TextStyle(color: slateGray.withOpacity(0.4), fontSize: 12, fontFamily: 'Cairo'),
                            prefixIcon: const Icon(Icons.search, color: goldenBrass, size: 20),
                            filled: true,
                            fillColor: darkNavy.withOpacity(0.5),
                            contentPadding: const EdgeInsets.symmetric(vertical: 8),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                              borderSide: BorderSide(color: Colors.white.withOpacity(0.05)),
                            ),
                            focusedBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                              borderSide: const BorderSide(color: goldenBrass),
                            ),
                          ),
                        ),
                        const SizedBox(height: 12),
                        // Scrollable List
                        Expanded(
                          child: ListView.builder(
                            controller: scrollController,
                            itemCount: filteredFaqs.length,
                            itemBuilder: (context, index) {
                              final item = filteredFaqs[index];
                              return Container(
                                margin: const EdgeInsets.only(bottom: 10),
                                padding: const EdgeInsets.all(12),
                                decoration: BoxDecoration(
                                  color: Colors.white.withOpacity(0.02),
                                  borderRadius: BorderRadius.circular(10),
                                  border: Border.all(color: Colors.white.withOpacity(0.04)),
                                ),
                                child: InkWell(
                                  onTap: () {
                                    Navigator.pop(context);
                                    _messageController.text = item['question'] ?? '';
                                    _handleSendMessage();
                                  },
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'س ${index + 1}: ${item['question']}',
                                        style: const TextStyle(
                                          color: goldenBrass,
                                          fontWeight: FontWeight.bold,
                                          fontSize: 12.5,
                                          fontFamily: 'Cairo',
                                        ),
                                      ),
                                      const SizedBox(height: 6),
                                      Text(
                                        item['answer'] ?? '',
                                        style: TextStyle(
                                          color: slateGray.withOpacity(0.8),
                                          fontSize: 11.5,
                                          height: 1.5,
                                          fontFamily: 'Cairo',
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    const Color darkNavy = Color(0xFF0D1B2A);
    const Color solidNavy = Color(0xFF1B263B);
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateGray = Color(0xFFE0E1DD);

    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: darkNavy,
        appBar: AppBar(
          backgroundColor: solidNavy,
          elevation: 4,
          iconTheme: const IconThemeData(color: goldenBrass),
          title: const Text(
            'مستشار الإسعاف الحربي الذكي',
            style: TextStyle(
              color: goldenBrass,
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.quiz_outlined, color: goldenBrass),
              tooltip: 'تصفح قاعدة الأسئلة الـ 200',
              onPressed: _showFaqBottomSheet,
            ).animate().scale(delay: 100.ms, duration: 300.ms),
            IconButton(
              icon: const Icon(Icons.share_outlined, color: goldenBrass),
              tooltip: 'تصدير المحادثة',
              onPressed: _exportConversation,
            ).animate().scale(delay: 200.ms, duration: 300.ms),
          ],
        ),
        body: Column(
          children: [
            Expanded(
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.all(16.0),
                itemCount: _messages.length,
                itemBuilder: (context, index) {
                  final msg = _messages[index];
                  final isUser = msg['isUser'] == true;
                  final isLatest = index == _messages.length - 1;
                  return _buildEnhancedBubble(msg, isUser, isLatest, goldenBrass, slateGray);
                },
              ),
            ),
            if (_isTyping)
              Padding(
                padding: const EdgeInsets.only(left: 16.0, right: 16.0, bottom: 8.0),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.05),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        'جاري فحص لوائح المعهد الطبي ومطابقة استفسارك التكتيكي...',
                        style: TextStyle(
                          color: goldenBrass.withOpacity(0.8),
                          fontSize: 11,
                          fontStyle: FontStyle.italic,
                          fontFamily: 'Cairo',
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            _buildInputRow(darkNavy, solidNavy, goldenBrass, slateGray),
          ],
        ),
      ),
    );
  }

  Widget _buildEnhancedBubble(Map<String, dynamic> msg, bool isUser, bool isLatest, Color gold, Color slate) {
    final String text = msg['text'] ?? '';
    final bubbleBg = isUser ? gold : Colors.white.withOpacity(0.04);
    final borderCol = isUser ? Colors.transparent : Colors.white.withOpacity(0.08);
    final textCol = isUser ? Colors.black : slate;

    final List<dynamic>? msgChoices = msg['choices'];
    final List<dynamic>? msgSteps = msg['steps'];
    final int currentStepIndex = msg['currentStepIndex'] ?? 0;
    final bool isSequential = msg['isSequential'] ?? false;

    return Align(
      alignment: isUser ? Alignment.centerLeft : Alignment.centerRight,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 6.0),
        constraints: const BoxConstraints(maxWidth: 320),
        child: Column(
          crossAxisAlignment: isUser ? CrossAxisAlignment.start : CrossAxisAlignment.end,
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
              decoration: BoxDecoration(
                color: bubbleBg,
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(16),
                  topRight: const Radius.circular(16),
                  bottomLeft: isUser ? const Radius.circular(0) : const Radius.circular(16),
                  bottomRight: isUser ? const Radius.circular(16) : const Radius.circular(0),
                ),
                border: Border.all(color: borderCol),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    text,
                    style: TextStyle(
                      color: textCol,
                      fontSize: 13,
                      fontFamily: 'Cairo',
                      height: 1.5,
                    ),
                  ),
                  if (!isUser && isSequential && msgSteps != null && msgSteps.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Divider(color: slate.withOpacity(0.1), height: 1),
                    const SizedBox(height: 6),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'الخطوة ${currentStepIndex + 1} من ${msgSteps.length}',
                          style: TextStyle(
                            color: gold.withOpacity(0.8),
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            fontFamily: 'Cairo',
                          ),
                        ),
                        SizedBox(
                          width: 80,
                          height: 4,
                          child: LinearProgressIndicator(
                            value: (currentStepIndex + 1) / msgSteps.length,
                            backgroundColor: Colors.white10,
                            valueColor: AlwaysStoppedAnimation<Color>(gold),
                          ),
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
            // Render step navigation triggers if sequential
            if (!isUser && isSequential && isLatest && msgSteps != null && currentStepIndex < msgSteps.length - 1) ...[
              Padding(
                padding: const EdgeInsets.only(top: 6.0),
                child: Wrap(
                  spacing: 6,
                  runSpacing: 4,
                  alignment: WrapAlignment.end,
                  children: [
                    ElevatedButton.icon(
                      onPressed: () => _handleNextStep(msgSteps, currentStepIndex + 1),
                      icon: const Icon(Icons.arrow_forward_rounded, size: 14, color: Colors.black),
                      label: const Text(
                        'الخطوة التالية ➡️',
                        style: TextStyle(fontFamily: 'Cairo', fontSize: 11, fontWeight: FontWeight.bold),
                      ),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: gold,
                        foregroundColor: Colors.black,
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        minimumSize: Size.zero,
                        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      ),
                    ),
                    OutlinedButton.icon(
                      onPressed: () => _handleAllStepsCombined(msgSteps, currentStepIndex + 1),
                      icon: const Icon(Icons.article_outlined, size: 14, color: Colors.white),
                      label: const Text(
                        'عرض الكل',
                        style: TextStyle(fontFamily: 'Cairo', fontSize: 11, color: Colors.white),
                      ),
                      style: OutlinedButton.styleFrom(
                        side: BorderSide(color: gold.withOpacity(0.5)),
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        minimumSize: Size.zero,
                        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      ),
                    ),
                  ],
                ),
              ),
            ],
            // Render alternative matching choices if any
            if (!isUser && isLatest && msgChoices != null && msgChoices.isNotEmpty) ...[
              Padding(
                padding: const EdgeInsets.only(top: 8.0, right: 4.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'أسئلة مشابهة قد تبحث عنها:',
                      style: TextStyle(
                        color: gold.withOpacity(0.9),
                        fontSize: 11,
                        fontFamily: 'Cairo',
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    ...msgChoices.map((choice) {
                      final q = choice['question'] ?? '';
                      return Container(
                        margin: const EdgeInsets.only(bottom: 5.0),
                        width: double.infinity,
                        child: OutlinedButton(
                          onPressed: () => _handleSendMessage(customText: q),
                          style: OutlinedButton.styleFrom(
                            side: BorderSide(color: Colors.white.withOpacity(0.08)),
                            backgroundColor: Colors.white.withOpacity(0.01),
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                            alignment: Alignment.centerRight,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(8),
                            ),
                          ),
                          child: Text(
                            q,
                            style: const TextStyle(
                              color: Colors.white70,
                              fontSize: 11,
                              fontFamily: 'Cairo',
                              height: 1.3,
                            ),
                            textAlign: TextAlign.right,
                          ),
                        ),
                      );
                    }).toList(),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    ).animate().fadeIn(duration: 300.ms).slideY(begin: 0.05, end: 0, duration: 300.ms);
  }

  Widget _buildMessageBubble(String text, bool isUser, Color gold, Color slate) {
    return Container(); // Deprecated in favor of _buildEnhancedBubble
  }

  Widget _buildInputRow(Color dark, Color solid, Color gold, Color slate) {
    return Container(
      padding: const EdgeInsets.all(12.0),
      decoration: BoxDecoration(
        color: solid,
        border: Border(
          top: BorderSide(color: Colors.white.withOpacity(0.05)),
        ),
      ),
      child: SafeArea(
        child: Row(
          children: [
            IconButton(
              icon: Icon(Icons.help_center_outlined, color: gold, size: 24),
              tooltip: 'الأسئلة الـ 200 الشائعة',
              onPressed: _showFaqBottomSheet,
            ).animate().scale(duration: 200.ms),
            const SizedBox(width: 4),
            Expanded(
              child: TextField(
                controller: _messageController,
                textInputAction: TextInputAction.send,
                onSubmitted: (_) => _handleSendMessage(),
                style: TextStyle(color: slate, fontSize: 13, fontFamily: 'Cairo'),
                decoration: InputDecoration(
                  hintText: 'اسأل عن MARCH، نقل الدم، بكتيريا الكزاز، الأتروبين...',
                  hintStyle: TextStyle(color: slate.withOpacity(0.4), fontSize: 12, fontFamily: 'Cairo'),
                  filled: true,
                  fillColor: dark.withOpacity(0.5),
                  contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(24),
                    borderSide: BorderSide(color: Colors.white.withOpacity(0.05)),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(24),
                    borderSide: BorderSide(color: gold.withOpacity(0.4)),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 8),
            IconButton(
              icon: Icon(Icons.send_rounded, color: gold),
              onPressed: () => _handleSendMessage(),
            ),
          ],
        ),
      ),
    );
  }
}
