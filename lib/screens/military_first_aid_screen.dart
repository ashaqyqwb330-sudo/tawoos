import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../data/military_first_aid_data.dart';
import '../data/military_first_aid_faq.dart';
import 'military_first_aid_chat_screen.dart';
import 'military_first_aid_dashboard.dart';

class MilitaryFirstAidScreen extends StatefulWidget {
  final int initialTab;
  const MilitaryFirstAidScreen({Key? key, this.initialTab = 0}) : super(key: key);

  @override
  State<MilitaryFirstAidScreen> createState() => _MilitaryFirstAidScreenState();
}

class _MilitaryFirstAidScreenState extends State<MilitaryFirstAidScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _faqSearchController = TextEditingController();
  String _faqSearchQuery = '';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this, initialIndex: widget.initialTab);
  }

  @override
  void dispose() {
    _faqSearchController.dispose();
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    const Color darkNavy = Color(0xFF0D1B2A);
    const Color solidNavy = Color(0xFF1B263B);
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateLight = Color(0xFFE0E1DD);

    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: darkNavy,
        appBar: AppBar(
          backgroundColor: solidNavy,
          elevation: 4,
          iconTheme: const IconThemeData(color: goldenBrass),
          title: const Text(
            'دبلوم الإسعاف الحربي الشامل',
            style: TextStyle(
              color: goldenBrass,
              fontSize: 18,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          bottom: TabBar(
            controller: _tabController,
            isScrollable: true,
            indicatorColor: goldenBrass,
            labelColor: goldenBrass,
            unselectedLabelColor: Colors.white60,
            labelStyle: const TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 13,
              fontFamily: 'Cairo',
            ),
            tabs: const [
              Tab(text: 'نظرة عامة'),
              Tab(text: 'الأهداف والمخرجات'),
              Tab(text: 'الخطة الدراسية'),
              Tab(text: 'القبول والتساؤلات'),
            ],
          ),
        ),
        body: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [solidNavy, darkNavy],
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
            ),
          ),
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildOverviewTab(goldenBrass, slateLight),
              _buildObjectivesTab(goldenBrass, slateLight),
              _buildCurriculumTab(goldenBrass, slateLight),
              _buildAdmissionTab(goldenBrass, slateLight),
            ],
          ),
        ).animate().fadeIn(duration: 500.ms).slideY(begin: 0.05, end: 0, duration: 500.ms),
        floatingActionButton: FloatingActionButton.extended(
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => const MilitaryFirstAidChatScreen(),
              ),
            );
          },
          backgroundColor: goldenBrass,
          foregroundColor: Colors.black,
          elevation: 6,
          shape: const RoundedCornerShape(16),
          icon: const Icon(Icons.chat_bubble_outline),
          label: const Text(
            'مستشار الدبلوم الذكي',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 13,
              fontFamily: 'Cairo',
            ),
          ),
        ).animate().scale(delay: 400.ms, duration: 400.ms, curve: Curves.easeOut),
      ),
    );
  }

  Widget _buildOverviewTab(Color gold, Color slate) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildCard(
            title: 'رسالة الكلية والنهج الأكاديمي الشامل',
            content: MilitaryFirstAidData.sections[0]['content'] ?? '',
            goldColor: gold,
            slateColor: slate,
            icon: Icons.school,
          ),
          const SizedBox(height: 16),
          _buildCard(
            title: 'رؤية وأهداف دبلوم الإسعاف الحربي مدرسة المقاتل',
            content: MilitaryFirstAidData.sections[1]['content'] ?? '',
            goldColor: gold,
            slateColor: slate,
            icon: Icons.remove_red_eye_outlined,
          ),
          const SizedBox(height: 16),
          _buildCard(
            title: 'مباركة وتوجيه المعهد العسكري',
            content: MilitaryFirstAidData.getMessage(),
            goldColor: gold,
            slateColor: slate,
            icon: Icons.star,
            isHighlight: true,
          ),
        ],
      ),
    );
  }

  Widget _buildObjectivesTab(Color gold, Color slate) {
    final objectives = MilitaryFirstAidData.getObjectives();
    final ilos = MilitaryFirstAidData.getILOs();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'الأهداف الإستراتيجية والأكاديمية للبرنامج',
            style: TextStyle(
              color: Color(0xFFC5A44E),
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ).animate().fadeIn(delay: 100.ms),
          const SizedBox(height: 8),
          ...objectives.map((obj) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 4.0),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.check_circle, color: gold, size: 20),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    obj,
                    style: TextStyle(color: slate, fontSize: 13, fontFamily: 'Cairo'),
                  ),
                ),
              ],
            ),
          )),
          const SizedBox(height: 24),
          const Text(
            'مخرجات التعلم المستهدفة (ILOs)',
            style: TextStyle(
              color: Color(0xFFC5A44E),
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ).animate().fadeIn(delay: 200.ms),
          const SizedBox(height: 8),
          ...ilos.map((ilo) => _buildIloItem(ilo, gold, slate)),
        ],
      ),
    );
  }

  Widget _buildIloItem(String text, Color gold, Color slate) {
    final parts = text.split(':');
    final tag = parts[0];
    final detail = parts.length > 1 ? parts[1] : '';

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 6.0),
      padding: const EdgeInsets.all(12.0),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.02),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.white.withOpacity(0.05), width: 1),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
            decoration: BoxDecoration(
              color: gold.withOpacity(0.15),
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: gold.withOpacity(0.4)),
            ),
            child: Text(
              tag,
              style: TextStyle(color: gold, fontSize: 11, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              detail,
              style: TextStyle(color: slate, fontSize: 13, fontFamily: 'Cairo'),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCurriculumTab(Color gold, Color slate) {
    return const SingleChildScrollView(
      padding: EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: MilitaryFirstAidDashboard(),
    );
  }

  Widget _buildAdmissionTab(Color gold, Color slate) {
    // Normalization helper
    String normalize(String text) {
      String clean = text.toLowerCase();
      clean = clean.replaceAll(RegExp(r'[أإآ]'), 'ا');
      clean = clean.replaceAll(RegExp(r'[ى]'), 'ي');
      clean = clean.replaceAll(RegExp(r'[ة]'), 'ه');
      clean = clean.replaceAll(RegExp(r'[ًٌٍَُِّْ]'), '');
      return clean.trim();
    }

    final query = normalize(_faqSearchQuery);
    final filteredFaqs = MilitaryFirstAidFaq.faq.where((faqItem) {
      if (query.isEmpty) return true;
      final q = normalize(faqItem['question'] ?? '');
      final a = normalize(faqItem['answer'] ?? '');
      return q.contains(query) || a.contains(query);
    }).toList();

    final List<Map<String, String>> displayedFaqs = _faqSearchQuery.isEmpty 
        ? filteredFaqs.take(10).toList() 
        : filteredFaqs;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildCard(
            title: 'شروط القبول ومرحلة فرز اللياقة الطبية العسكرية',
            content: MilitaryFirstAidData.sections[3]['content'] ?? '',
            goldColor: gold,
            slateColor: slate,
            icon: Icons.verified_user_sharp,
          ),
          const SizedBox(height: 24),
          Row(
            children: [
              Icon(Icons.question_answer_outlined, color: gold),
              const SizedBox(width: 8),
              const Text(
                'قاعدة نقاش الدبلوم والـ 200 سؤال',
                style: TextStyle(
                  color: Color(0xFFC5A44E),
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  fontFamily: 'Cairo',
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          // Inline FAQ search bar
          TextField(
            controller: _faqSearchController,
            onChanged: (val) {
              setState(() {
                _faqSearchQuery = val;
              });
            },
            style: const TextStyle(color: Colors.white, fontSize: 13, fontFamily: 'Cairo'),
            decoration: InputDecoration(
              hintText: 'ابحث في الـ 200 سؤال الشائع والبروتوكولات الطبية...',
              hintStyle: TextStyle(color: slate.withOpacity(0.4), fontSize: 12, fontFamily: 'Cairo'),
              prefixIcon: const Icon(Icons.search, color: Color(0xFFC5A44E), size: 20),
              suffixIcon: _faqSearchQuery.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.clear, color: Color(0xFFC5A44E), size: 18),
                      onPressed: () {
                        _faqSearchController.clear();
                        setState(() {
                          _faqSearchQuery = '';
                        });
                      },
                    )
                  : null,
              filled: true,
              fillColor: Colors.white.withOpacity(0.02),
              contentPadding: const EdgeInsets.symmetric(vertical: 8),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(color: Colors.white.withOpacity(0.08)),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: Color(0xFFC5A44E)),
              ),
            ),
          ),
          const SizedBox(height: 16),
          if (_faqSearchQuery.isEmpty)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text(
                'نعرض حالياً الأسئلة الـ 10 الحيوية كعينة. أدخل أحرفاً للبحث والتصفح التلقائي في كافة الأسئلة الـ 200.',
                style: TextStyle(
                  color: slate.withOpacity(0.5),
                  fontSize: 11,
                  fontStyle: FontStyle.italic,
                  fontFamily: 'Cairo',
                ),
              ),
            ),
          if (displayedFaqs.isEmpty)
            Container(
              padding: const EdgeInsets.all(24),
              alignment: Alignment.center,
              child: Column(
                children: [
                  Icon(Icons.search_off, color: gold.withOpacity(0.6), size: 36),
                  const SizedBox(height: 8),
                  const Text(
                    'لا توجد أسئلة مطابقة للاستعلام.',
                    style: TextStyle(color: Colors.white70, fontSize: 12, fontFamily: 'Cairo'),
                  ),
                ],
              ),
            ),
          ...displayedFaqs.map((faqItem) => Container(
            margin: const EdgeInsets.only(bottom: 12),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.03),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.white.withOpacity(0.05)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.help_outline, color: Color(0xFFC5A44E), size: 16),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        faqItem['question'] ?? '',
                        style: TextStyle(
                          color: gold,
                          fontSize: 13,
                          fontWeight: FontWeight.bold,
                          fontFamily: 'Cairo',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Padding(
                  padding: const EdgeInsets.only(right: 24),
                  child: Text(
                    faqItem['answer'] ?? '',
                    style: TextStyle(
                      color: slate.withOpacity(0.9),
                      fontSize: 12,
                      fontFamily: 'Cairo',
                      height: 1.5,
                    ),
                  ),
                ),
              ],
            ),
          )),
        ],
      ),
    );
  }

  Widget _buildCard({
    required String title,
    required String content,
    required Color goldColor,
    required Color slateColor,
    required IconData icon,
    bool isHighlight = false,
  }) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: isHighlight ? goldColor.withOpacity(0.08) : Colors.white.withOpacity(0.03),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isHighlight ? goldColor.withOpacity(0.3) : Colors.white.withOpacity(0.08),
          width: isHighlight ? 1.5 : 1,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: goldColor, size: 22),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  title,
                  style: TextStyle(
                    color: goldColor,
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Cairo',
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            content,
            style: TextStyle(
              color: slateColor,
              fontSize: 12.5,
              height: 1.6,
              fontFamily: 'Cairo',
            ),
          ),
        ],
      ),
    ).animate().fadeIn(duration: 400.ms).slideX(begin: 0.05, end: 0, duration: 400.ms);
  }
}

class RoundedCornerShape extends OutlinedBorder {
  final double radius;

  const RoundedCornerShape(this.radius);

  @override
  OutlinedBorder copyWith({BorderSide? side}) {
    return RoundedCornerShape(radius);
  }

  @override
  Path getInnerPath(Rect rect, {TextDirection? textDirection}) {
    return Path()..addRRect(RRect.fromRectAndRadius(rect, Radius.circular(radius)));
  }

  @override
  Path getOuterPath(Rect rect, {TextDirection? textDirection}) {
    return Path()..addRRect(RRect.fromRectAndRadius(rect, Radius.circular(radius)));
  }

  @override
  void paint(Canvas canvas, Rect rect, {TextDirection? textDirection}) {}

  @override
  ShapeBorder scale(double t) {
    return RoundedCornerShape(radius * t);
  }
}
