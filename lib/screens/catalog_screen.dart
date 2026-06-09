import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'bachelor_medicine_screen.dart';
import 'military_first_aid_screen.dart';

class CatalogScreen extends StatelessWidget {
  const CatalogScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    const Color darkNavy = Color(0xFF0D1B2A);
    const Color solidNavy = Color(0xFF1B263B);
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateColor = Color(0xFFE0E1DD);

    final catalogItems = [
      {
        'title': 'دبلوم التمريض الميداني',
        'id': 'nursing_diploma',
        'icon': Icons.healing,
        'active': false,
      },
      {
        'title': 'دبلوم التخدير الميداني',
        'id': 'anesthesia_diploma',
        'icon': Icons.medical_services,
        'active': false,
      },
      {
        'title': 'بكالوريوس الطب البشري والجراحة العسكرية',
        'id': 'general_medicine',
        'icon': Icons.health_and_safety,
        'active': true,
      },
      {
        'title': 'دبلوم الإسعاف الحربي الشامل',
        'id': 'military_first_aid',
        'icon': Icons.military_tech,
        'active': true,
      },
      {
        'title': 'بكالوريوس مختبرات عسكرية',
        'id': 'military_lab',
        'icon': Icons.science,
        'active': false,
      },
      {
        'title': 'بكالوريوس طب وقائي عسكري',
        'id': 'preventive_medicine',
        'icon': Icons.shield,
        'active': false,
      },
      {
        'title': 'دبلوم مساعد طبي عسكري',
        'id': 'assistant_medicine',
        'icon': Icons.group_add,
        'active': false,
      },
      {
        'title': 'دورة إسعاف المقاتل',
        'id': 'combat_care',
        'icon': Icons.local_hospital,
        'active': false,
      },
      {
        'title': 'دورة إسعاف متقدم تكتيكي',
        'id': 'tactical_ambulance',
        'icon': Icons.airline_seat_flat_angled,
        'active': false,
      },
    ];

    return Directionality(
      textDirection: TextDirection.rtl,
      child: Container(
        color: darkNavy,
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'دليل البرامج والدبلومات العسكرية المعتمدة',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: goldenBrass,
                fontSize: 15,
                fontFamily: 'Cairo',
              ),
            ).animate().fadeIn(duration: 400.ms),
            const SizedBox(height: 12),
            Expanded(
              child: GridView.builder(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  crossAxisSpacing: 10,
                  mainAxisSpacing: 10,
                  childAspectRatio: 0.85,
                ),
                itemCount: catalogItems.length,
                itemBuilder: (context, index) {
                  final item = catalogItems[index];
                  final isActive = item['active'] == true;

                  return InkWell(
                    onTap: () {
                      if (isActive) {
                        Widget target = const BachelorMedicineScreen();
                        if (item['id'] == 'military_first_aid') {
                          target = const MilitaryFirstAidScreen();
                        }
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => target,
                          ),
                        );
                      } else {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              'برنامج ${item['title']} مفعل سريرياً في النظام ولكن تحت الصيانة التقنية المؤقتة لمطابقة جداول الجبهات.',
                              style: const TextStyle(fontFamily: 'Cairo', fontSize: 11),
                            ),
                            backgroundColor: solidNavy,
                            duration: const Duration(seconds: 3),
                          ),
                        );
                      }
                    },
                    borderRadius: BorderRadius.circular(16),
                    child: Container(
                      padding: const EdgeInsets.all(8.0),
                      decoration: BoxDecoration(
                        color: isActive ? goldenBrass.withOpacity(0.08) : Colors.white.copy(alpha: 0.02),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: isActive ? goldenBrass.withOpacity(0.4) : Colors.white.copy(alpha: 0.05),
                          width: isActive ? 1.5 : 1,
                        ),
                      ),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: isActive ? goldenBrass.withOpacity(0.15) : Colors.white.copy(alpha: 0.04),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              item['icon'] as IconData,
                              color: isActive ? goldenBrass : slateColor.withOpacity(0.4),
                              size: 24,
                            ),
                          ),
                          const SizedBox(height: 10),
                          Text(
                            item['title'] as String,
                            textAlign: TextAlign.center,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                              color: isActive ? goldenBrass : slateColor.withOpacity(0.7),
                              fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
                              fontSize: 10,
                              fontFamily: 'Cairo',
                              height: 1.3,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ).animate().fadeIn(delay: (index * 50).ms, duration: 300.ms).scale(begin: const Offset(0.95, 0.95), end: const Offset(1, 1));
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
