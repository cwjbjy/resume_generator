import { Document, Page, StyleSheet, Text, View } from '@react-pdf/renderer';
import { type Content, type Template } from '../../../types/index';
import { sections } from '../model/resumeSections';
import './fonts';
// Explicit Unicode break opportunities keep Chinese paragraphs wrapping without
// inserting visible English hyphens into the original text.
const pdfText = (value = '') => value.replace(/([\u2e80-\u9fff])(?=[\u2e80-\u9fff])/g, '$1\u200a');
const styles = StyleSheet.create({
  page: {
    fontFamily: 'NotoSansSC',
    fontSize: 10,
    lineHeight: 1.65,
    padding: 38,
    paddingBottom: 45,
    color: '#28303e',
  },
  head: { paddingBottom: 20, marginBottom: 4, borderBottomWidth: 2 },
  name: { fontSize: 27, fontWeight: 700, lineHeight: 1.5, marginBottom: 4 },
  job: { fontSize: 12, lineHeight: 1.5, marginBottom: 7 },
  contact: { fontSize: 9, lineHeight: 1.5, color: '#707887' },
  section: { marginTop: 16 },
  heading: {
    fontSize: 12,
    fontWeight: 700,
    lineHeight: 1.5,
    paddingBottom: 5,
    marginBottom: 8,
    borderBottomWidth: 0.6,
    borderBottomColor: '#dce1e8',
  },
  entry: { marginBottom: 10 },
  row: { flexDirection: 'row', justifyContent: 'space-between', gap: 12 },
  title: { fontSize: 11, lineHeight: 1.5, fontWeight: 700, flexShrink: 1 },
  date: { fontSize: 8, lineHeight: 1.5, color: '#808794' },
  subtitle: { fontSize: 9, lineHeight: 1.5, color: '#6d7480', marginTop: 2, marginBottom: 4 },
  bullet: { flexDirection: 'row', gap: 5 },
  dot: { width: 5 },
  bulletText: { flex: 1 },
  skills: { flexDirection: 'row', flexWrap: 'wrap', gap: 7 },
  skill: { fontSize: 9, backgroundColor: '#f0f3f6', padding: '3 8' },
  footer: {
    position: 'absolute',
    bottom: 20,
    left: 38,
    right: 38,
    height: 15,
    color: '#9aa4b3',
    fontSize: 8,
    lineHeight: 1.4,
    textAlign: 'center',
  },
});
export default function PdfDocument({
  content,
  template,
  watermark,
}: {
  content: Content;
  template: Template;
  watermark: boolean;
}) {
  const colored = template.style === 'modern';
  return (
    <Document title={`${content.basic.name}的简历`} author={content.basic.name} language="zh-CN">
      <Page size="A4" style={styles.page}>
        <View
          style={[
            styles.head,
            { borderBottomColor: template.color },
            ...(colored
              ? [
                  {
                    backgroundColor: template.color,
                    color: '#fff',
                    marginTop: -38,
                    marginLeft: -38,
                    marginRight: -38,
                    marginBottom: 8,
                    paddingTop: 38,
                    paddingLeft: 38,
                    paddingRight: 38,
                    paddingBottom: 20,
                  },
                ]
              : []),
            ...(template.style === 'mono' ? [{ alignItems: 'center' as const }] : []),
            ...(template.style === 'fresh'
              ? [{ borderLeftWidth: 5, borderLeftColor: template.color, paddingLeft: 14 }]
              : []),
          ]}
        >
          <Text style={styles.name}>{pdfText(content.basic.name || '你的姓名')}</Text>
          <Text style={styles.job}>求职意向 / {pdfText(content.basic.job)}</Text>
          <Text style={[styles.contact, { color: colored ? '#ffffff' : '#707887' }]}>
            {pdfText(
              [content.basic.phone, content.basic.email, content.basic.city]
                .filter(Boolean)
                .join('  ·  '),
            )}
          </Text>
        </View>
        {sections(content).map((section) =>
          section.text || section.entries?.length || section.skills?.length ? (
            <View key={section.key} style={styles.section}>
              <Text
                style={[
                  styles.heading,
                  { color: template.color },
                  ...(template.style === 'classic'
                    ? [{ backgroundColor: '#f0f2f5', borderBottomWidth: 0, padding: '4 8' }]
                    : []),
                  ...(template.style === 'mono' ? [{ borderBottomColor: '#222' }] : []),
                ]}
                minPresenceAhead={45}
              >
                {section.title}
              </Text>
              {section.text && <Text>{pdfText(section.text)}</Text>}
              {section.entries?.map((entry) => (
                <View key={entry.id} style={styles.entry}>
                  <View style={styles.row} minPresenceAhead={35}>
                    <Text style={styles.title}>
                      {pdfText(entry.school || entry.company || entry.name)}
                    </Text>
                    <Text style={styles.date}>
                      {entry.start} {entry.start && '—'} {entry.end || '至今'}
                    </Text>
                  </View>
                  <Text style={styles.subtitle}>
                    {pdfText([entry.role, entry.major, entry.degree].filter(Boolean).join(' / '))}
                  </Text>
                  {entry.desc
                    ?.split('\n')
                    .filter(Boolean)
                    .map((line, i) => (
                      <View key={i} style={styles.bullet}>
                        <Text style={styles.dot}>•</Text>
                        <Text style={styles.bulletText}>{pdfText(line)}</Text>
                      </View>
                    ))}
                </View>
              ))}
              {section.skills && (
                <View style={styles.skills}>
                  {section.skills.map((skill) => (
                    <Text key={skill} style={styles.skill}>
                      {skill}
                    </Text>
                  ))}
                </View>
              )}
            </View>
          ) : null,
        )}
        {watermark && (
          <Text fixed style={styles.footer}>
            简历工坊 · 免费版
          </Text>
        )}
      </Page>
    </Document>
  );
}
