import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../data/bachelor_medicine_data.dart';
import '../data/bachelor_medicine_faq.dart';
import 'bachelor_medicine_chat_screen.dart';
import 'study_plan_dashboard.dart';

class BachelorMedicineScreen extends StatefulWidget {
  const BachelorMedicineScreen({Key? key}) : super(key: key);

  @override
  State<BachelorMedicineScreen> createState() => _BachelorMedicineScreenState();
}

class _BachelorMedicineScreenState extends State<BachelorMedicineScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  int _selectedYearIndex = 0;
  String? _selectedSemesterName;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
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
            'بكالوريوس الطب والجراحة العسكرية',
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
                builder: (context) => const BachelorMedicineChatScreen(),
              ),
            );
          },
          backgroundColor: goldenBrass,
          foregroundColor: Colors.black,
          elevation: 6,
          shape: RoundedCornerShape(16),
          icon: const Icon(Icons.chat_bubble_outline),
          label: const Text(
            'مستشار البرنامج الذكي',
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
            title: 'رسالة الكلية والنهج الأكاديمي',
            content: BachelorMedicineData.sections[0]['content'] ?? '',
            goldColor: gold,
            slateColor: slate,
            icon: Icons.school,
          ),
          const SizedBox(height: 16),
          _buildCard(
            title: 'رؤية وأهداف بكالوريوس الجراحة',
            content: BachelorMedicineData.sections[1]['content'] ?? '',
            goldColor: gold,
            slateColor: slate,
            icon: Icons.remove_red_eye_outlined,
          ),
          const SizedBox(height: 16),
          _buildCard(
            title: 'كلمة ورسالة الشهيد الدكتور زيد طاووس',
            content: BachelorMedicineData.getMessage(),
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
    final objectives = BachelorMedicineData.getObjectives();
    final ilos = BachelorMedicineData.getILOs();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'الأهداف الإستراتيجية والأكاديمية',
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
        color: Colors.white.copy(alpha: 0.02),
        borderRadius: BorderRadius.circular(10),
        border: BorderBorder(gold),
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

  Border? BorderBorder(Color gold) {
    return Border.all(color: Colors.white.copy(alpha: 0.05), width: 1);
  }

  Widget _buildCurriculumTab(Color gold, Color slate) {
    return const SingleChildScrollView(
      padding: EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: StudyPlanDashboard(),
    );
  }

  Widget _buildAdmissionTab(Color gold, Color slate) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildCard(
            title: 'شروط القبول ومرحلة الفرز البدني الطبي',
            content: BachelorMedicineData.getAdmission(),
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
                'الأسئلة الأكاديمية الشائعة',
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
          ...BachelorMedicineFaq.faq.take(4).map((faqItem) => Container(
            margin: const EdgeInsets.only(bottom: 12),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: Colors.white.copy(alpha: 0.03),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.white.copy(alpha: 0.05)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  faqItem['question'] ?? '',
                  style: TextStyle(
                    color: gold,
                    fontSize: 13,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Cairo',
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  faqItem['answer'] ?? '',
                  style: TextStyle(
                    color: slate.withOpacity(0.9),
                    fontSize: 12,
                    fontFamily: 'Cairo',
                    height: 1.5,
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
        color: isHighlight ? goldColor.withOpacity(0.08) : Colors.white.copy(alpha: 0.03),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isHighlight ? goldColor.withOpacity(0.3) : Colors.white.copy(alpha: 0.08),
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
