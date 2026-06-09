import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'bachelor_medicine_screen.dart';
import 'military_first_aid_screen.dart';

class AppDrawer extends StatefulWidget {
  const AppDrawer({Key? key}) : super(key: key);

  @override
  State<AppDrawer> createState() => _AppDrawerState();
}

class _AppDrawerState extends State<AppDrawer> {
  int _activeDrawerTab = 0; // 0 = Academic Tree, 1 = Archive Log
  bool _bachelorsExpanded = false;
  bool _diplomasExpanded = true;
  bool _mfaSectionExpanded = true;
  bool _coursesExpanded = false;
  bool _gradStudiesExpanded = false;

  @override
  Widget build(BuildContext context) {
    const Color darkNavy = Color(0xFF0D1B2A);
    const Color solidNavy = Color(0xFF1B263B);
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateColor = Color(0xFFE0E1DD);

    return Drawer(
      backgroundColor: darkNavy,
      child: Column(
        children: [
          // Drawer Header with Slate-to-Navy gradient and Golden emblem
          Container(
            padding: const EdgeInsets.fromLTRB(20, 50, 20, 20),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFF162636), darkNavy],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: goldenBrass.withOpacity(0.15),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(
                    Icons.school,
                    color: goldenBrass,
                    size: 28,
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  'مرشد الشهيد د. زيد طاووس',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                    color: goldenBrass,
                    fontFamily: 'Cairo',
                  ),
                ),
                const Text(
                  'كلية الطب والعلوم الصحية العسكرية',
                  style: TextStyle(
                    fontSize: 10,
                    color: Colors.white70,
                    fontFamily: 'Cairo',
                  ),
                ),
              ],
            ),
          ),

          // Toggle Tabs
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: Container(
              padding: const EdgeInsets.all(2.0),
              decoration: BoxDecoration(
                color: Colors.white.copy(alpha: 0.02),
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: Colors.white.copy(alpha: 0.05)),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: InkWell(
                      onTap: () => setState(() => _activeDrawerTab = 0),
                      borderRadius: BorderRadius.circular(24),
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 8.0),
                        decoration: BoxDecoration(
                          color: _activeDrawerTab == 0 ? goldenBrass : Colors.transparent,
                          borderRadius: BorderRadius.circular(24),
                        ),
                        child: Text(
                          'الشجرة الأكاديمية',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: _activeDrawerTab == 0 ? Colors.black : slateColor,
                            fontFamily: 'Cairo',
                          ),
                        ),
                      ),
                    ),
                  ),
                  Expanded(
                    child: InkWell(
                      onTap: () => setState(() => _activeDrawerTab = 1),
                      borderRadius: BorderRadius.circular(24),
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 8.0),
                        decoration: BoxDecoration(
                          color: _activeDrawerTab == 1 ? goldenBrass : Colors.transparent,
                          borderRadius: BorderRadius.circular(24),
                        ),
                        child: Text(
                          'المحفوظات التكتيكية',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: _activeDrawerTab == 1 ? Colors.black : slateColor,
                            fontFamily: 'Cairo',
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const Divider(color: Colors.white10, height: 1),

          // Tab Contents
          Expanded(
            child: _activeDrawerTab == 0
                ? _buildAcademicTree(goldenBrass, slateColor)
                : _buildArchiveTab(goldenBrass, slateColor),
          ),

          // Footer
          const Padding(
            padding: EdgeInsets.all(16.0),
            child: Text(
              'تصديق عسكري - نظام الكلية المحمي',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 9,
                color: Colors.white30,
                fontWeight: FontWeight.light,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAcademicTree(Color gold, Color slate) {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      children: [
        // Category 1: Bachelors
        _buildSectionHeader(
          title: 'برامج الكلية (البكالوريوس)',
          expanded: _bachelorsExpanded,
          onToggle: () => setState(() => _bachelorsExpanded = !_bachelorsExpanded),
          gold: gold,
        ),
        if (_bachelorsExpanded)
          _buildSubItem('بكالوريوس الطب البشري والجراحة العسكرية', gold, true, () {
            Navigator.pop(context); // Close Drawer
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => const BachelorMedicineScreen()),
            );
          }),
        if (_bachelorsExpanded) _buildSubItem('بكالوريوس مختبرات عسكرية', gold, false, null),
        if (_bachelorsExpanded) _buildSubItem('بكالوريوس طب وقائي عسكري', gold, false, null),

        const SizedBox(height: 8),

        // Category 2: Diplomas
        _buildSectionHeader(
          title: 'برامج المعهد (الدبلومات)',
          expanded: _diplomasExpanded,
          onToggle: () => setState(() => _diplomasExpanded = !_diplomasExpanded),
          gold: gold,
        ),
        if (_diplomasExpanded)
          _buildSubItem('دبلوم الإسعاف الحربي الشامل', gold, true, () {
            Navigator.pop(context); // Close Drawer
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => const MilitaryFirstAidScreen()),
            );
          }),
        if (_diplomasExpanded) _buildSubItem('دبلوم التمريض الميداني المتقدم', gold, false, null),
        if (_diplomasExpanded) _buildSubItem('دبلوم التخدير الميداني المتقدم', gold, false, null),
        if (_diplomasExpanded) _buildSubItem('دبلوم مساعد طبي عسكري', gold, false, null),

        const SizedBox(height: 8),

        // Dedicated Section: Military First Aid Diploma Gateway
        _buildSectionHeader(
          title: 'دبلوم الإسعاف الحربي الشامل',
          expanded: _mfaSectionExpanded,
          onToggle: () => setState(() => _mfaSectionExpanded = !_mfaSectionExpanded),
          gold: gold,
        ),
        if (_mfaSectionExpanded) ...[
          _buildSubItem('لوحة تحكم ومنهج الدبلوم', gold, true, () {
            Navigator.pop(context); // Close Drawer
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => const MilitaryFirstAidScreen(initialTab: 2),
              ),
            );
          }),
          _buildSubItem('قائمة مطابقة شروط القبول', gold, true, () {
            Navigator.pop(context); // Close Drawer
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => const MilitaryFirstAidScreen(initialTab: 3),
              ),
            );
          }),
          _buildSubItem('مكتبة الأسئلة المتوقعة (200 سؤال)', gold, true, () {
            Navigator.pop(context); // Close Drawer
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => const MilitaryFirstAidScreen(initialTab: 3),
              ),
            );
          }),
        ],

        const SizedBox(height: 8),

        // Category 3: Training Courses
        _buildSectionHeader(
          title: 'برامج الدورات التدريبية والترشيح',
          expanded: _coursesExpanded,
          onToggle: () => setState(() => _coursesExpanded = !_coursesExpanded),
          gold: gold,
        ),
        if (_coursesExpanded) _buildSubItem('دورة إسعاف المقاتل التكتيكي', gold, false, null),
        if (_coursesExpanded) _buildSubItem('دورة إسعاف متقدم تكتيكي', gold, false, null),

        const SizedBox(height: 8),

        // Category 4: Postgraduate
        _buildSectionHeader(
          title: 'الدراسات الأكاديمية العليا',
          expanded: _gradStudiesExpanded,
          onToggle: () => setState(() => _gradStudiesExpanded = !_gradStudiesExpanded),
          gold: gold,
        ),
        if (_gradStudiesExpanded) _buildSubItem('جراحة الحروب والكسور الجسيمة', gold, false, null),
      ],
    );
  }

  Widget _buildSectionHeader({
    required String title,
    required bool expanded,
    required VoidCallback onToggle,
    required Color gold,
  }) {
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 4),
      onTap: onToggle,
      leading: Container(
        width: 6,
        height: 6,
        decoration: BoxDecoration(
          color: expanded ? gold : gold.withOpacity(0.5),
          shape: BoxShape.circle,
        ),
      ),
      title: Text(
        title,
        style: TextStyle(
          color: gold,
          fontSize: 12,
          fontWeight: FontWeight.bold,
          fontFamily: 'Cairo',
        ),
      ),
      trailing: Icon(
        expanded ? Icons.keyboard_arrow_down : Icons.keyboard_arrow_up,
        color: gold.withOpacity(0.6),
        size: 16,
      ),
    );
  }

  Widget _buildSubItem(String title, Color gold, bool isActive, VoidCallback? onTap) {
    return Container(
      margin: const EdgeInsets.only(left: 12, bottom: 2),
      decoration: BoxDecoration(
        color: isActive ? gold.withOpacity(0.05) : Colors.transparent,
        borderRadius: BorderRadius.circular(8),
      ),
      child: ListTile(
        visualDensity: const VisualDensity(vertical: -3),
        dense: true,
        onTap: onTap,
        horizontalTitleGap: -8,
        leading: Icon(
          Icons.arrow_left,
          color: isActive ? gold : Colors.white24,
          size: 18,
        ),
        title: Text(
          title,
          style: TextStyle(
            color: isActive ? gold : Colors.white70,
            fontSize: 11,
            fontFamily: 'Cairo',
            fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
          ),
        ),
      ),
    );
  }

  Widget _buildArchiveTab(Color gold, Color slate) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.history,
            color: gold.withOpacity(0.2),
            size: 48,
          ),
          const SizedBox(height: 8),
          const Text(
            'لا توجد محفوظات تكتيكية حالية',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 11,
              color: Colors.white30,
              fontFamily: 'Cairo',
            ),
          ),
        ],
      ),
    );
  }
}
