
# -*- coding: utf-8 -*-
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm, cm
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.colors import HexColor, white, black, Color
from reportlab.lib.enums import TA_LEFT, TA_CENTER, TA_JUSTIFY
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, 
    Table, TableStyle, PageBreak, Frame, PageTemplate, BaseDocTemplate, Image, NextPageTemplate)
from reportlab.platypus.flowables import HRFlowable
from reportlab.platypus import Flowable
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib import colors
import os, math

# Register fonts
font_path = "C:\\Windows\\Fonts\\msyh.ttc"
font_bold_path = "C:\\Windows\\Fonts\\msyhbd.ttc"
pdfmetrics.registerFont(TTFont('YaHei', font_path))
pdfmetrics.registerFont(TTFont('YaHei-Bold', font_bold_path))

# Colors
BLUE_TITLE = HexColor('#1a5276')
BLUE_HEADER = HexColor('#2c7ba8')
BLUE_LIGHT = HexColor('#d4e6f1')
BLUE_MID = HexColor('#85c1e9')
BLUE_TABLE_HEADER = HexColor('#2980b9')
BLUE_STEP = HexColor('#3498db')
BLUE_STEP_DARK = HexColor('#2471a3')
GRAY_BG = HexColor('#f2f3f4')
GRAY_TEXT = HexColor('#5d6d7e')
ORANGE_STEP = HexColor('#e67e22')
GREEN_STEP = HexColor('#27ae60')
RED_STEP = HexColor('#e74c3c')

output_path = "D:\\work\\proj2\\docs\\_tmp_gen_v235511.pdf"

PAGE_W, PAGE_H = A4

# ----- Flow Chart -----
class FlowChart(Flowable):
    def __init__(self, width, height):
        Flowable.__init__(self)
        self.width = width
        self.height = height
        
    def draw(self):
        c = self.canv
        w = self.width
        h = self.height
        
        box_colors = {
            'insure': HexColor('#2c7ba8'),
            'platform': HexColor('#3498db'),
            'external': HexColor('#27ae60'),
            'billing': HexColor('#e67e22'),
        }
        
        bx, by = 72, 28
        
        # Row 1
        steps = [
            (20, h-30, "\u4fdd\u9669\u516c\u53f8", "\u53d1\u8d77\u67e5\u8be2", 'insure'),
            (112, h-30, "\u5e73\u53f0\u7f51\u5173", "AppKey\u8ba4\u8bc1", 'platform'),
            (204, h-30, "\u8ba1\u8d39\u5f15\u64ce", "\u9884\u7b97\u9884\u7559", 'billing'),
            (296, h-30, "\u6570\u636e\u8def\u7531", "\u5339\u914d\u6570\u636e\u6e90", 'platform'),
        ]
        steps2 = [
            (80, h-90, "\u5916\u90e8\u6570\u636e\u6e90", "\u539f\u59cb\u533b\u7597\u6570\u636e", 'external'),
            (172, h-90, "\u8131\u654f\u5f15\u64ce", "\u6570\u636e\u8131\u654f", 'platform'),
            (264, h-90, "\u7ed3\u679c\u7ec4\u88c5", "JSON\u683c\u5f0f\u5316", 'platform'),
        ]
        steps3 = [
            (130, h-150, "\u8ba1\u8d39\u5f15\u64ce", "\u786e\u8ba4\u5165\u8d26", 'billing'),
            (222, h-150, "\u4fdd\u9669\u516c\u53f8", "\u8fd4\u56de\u7ed3\u679c", 'insure'),
        ]
        
        def draw_box(x, y, main_text, sub_text, color_type):
            col = box_colors[color_type]
            # Shadow
            c.setFillColor(HexColor('#bdc3c7'))
            c.roundRect(x+1.5, y-1.5, bx, by, 3, fill=1, stroke=0)
            # Main box
            c.setFillColor(col)
            c.setStrokeColor(HexColor('#1a5276'))
            c.setLineWidth(0.5)
            c.roundRect(x, y, bx, by, 3, fill=1, stroke=1)
            # Text
            c.setFillColor(white)
            c.setFont('YaHei-Bold', 9)
            c.drawCentredString(x + bx/2, y + by - 16, main_text)
            c.setFont('YaHei', 7)
            c.drawCentredString(x + bx/2, y + 6, sub_text)
        
        def draw_arrow(x1, y1, x2, y2, color_type='platform'):
            col = box_colors[color_type]
            c.setStrokeColor(col)
            c.setLineWidth(1.5)
            c.setFillColor(col)
            
            cx1 = x1; cy1 = y1
            cx2 = x2; cy2 = y2
            a = math.atan2(cy2 - cy1, cx2 - cx1)
            
            # Line
            c.line(cx1, cy1, cx2, cy2)
            
            # Arrow head
            arr_sz = 5
            p = c.beginPath()
            p.moveTo(cx2, cy2)
            p.lineTo(cx2 - arr_sz * math.cos(a - 0.4), cy2 - arr_sz * math.sin(a - 0.4))
            p.lineTo(cx2 - arr_sz * math.cos(a + 0.4), cy2 + arr_sz * math.sin(a + 0.4))
            p.close()
            c.drawPath(p, stroke=1, fill=1)
        
        # Right edge of each box
        def right(x): return x + bx
        def mid_y(y): return y + by/2
        def mid_x(x): return x + bx/2
        def top(y): return y + by
        def left(x): return x
        
        # Row 1 horizontal connections
        for i in range(len(steps)-1):
            x1, y1, _, _, t1 = steps[i]
            x2, y2, _, _, t2 = steps[i+1]
            draw_arrow(right(x1), mid_y(y1), x2, mid_y(y2), t2)
        
        # Row 1 to Row 2 connections
        # Step 3 (预算预留) -> Step 5 (原始数据)
        x3, y3, _, _, t3 = steps[2]
        x5, y5, _, _, t5 = steps2[0]
        draw_arrow(mid_x(x3), y3, mid_x(x5), top(y5), t5)
        
        # Step 4 (数据路由) -> Step 5 (原始数据)
        x4, y4, _, _, t4 = steps[3]
        draw_arrow(mid_x(x4), y4, mid_x(x5), top(y5), t5)
        
        # Row 2 horizontal
        for i in range(len(steps2)-1):
            x1, y1, _, _, t1 = steps2[i]
            x2, y2, _, _, t2 = steps2[i+1]
            draw_arrow(right(x1), mid_y(y1), x2, mid_y(y2), t2)
        
        # Row 2 -> Row 3: Step 7 (结果组装) -> Step 8 (确认入账)
        x_27, y_27, _, _, t_27 = steps2[2]
        x_38, y_38, _, _, t_38 = steps3[0]
        draw_arrow(mid_x(x_27), y_27, mid_x(x_38), top(y_38), t_38)
        
        # Row 3 horizontal
        x_38b, y_38b, _, _, t_38b = steps3[0]
        x_39, y_39, _, _, t_39 = steps3[1]
        draw_arrow(right(x_38b), mid_y(y_38b), x_39, mid_y(y_39), t_39)
        
        # Connection: Step 2 (AppKey认证) -> Step 6 (脱敏引擎) - process flow
        # Actually this is handled through the natural flow. Add connection from
        # Step 2 to Step 3 (already done via horizontal row 1)
        # and from Step 2 -> Step 3 -> Step 4 -> etc.
        
        # Draw all boxes
        for x, y, t, s, ct in steps:
            draw_box(x, y, t, s, ct)
        for x, y, t, s, ct in steps2:
            draw_box(x, y, t, s, ct)
        for x, y, t, s, ct in steps3:
            draw_box(x, y, t, s, ct)
        
        # Step numbers
        c.setFont('YaHei', 6)
        c.setFillColor(GRAY_TEXT)
        nums = ["\u2460", "\u2461", "\u2462", "\u2463", "\u2464", "\u2465", "\u2466", "\u2467", "\u2468"]
        all_steps = steps + steps2 + steps3

        # Sanity check
        for i, (x, y, _, _, _) in enumerate(all_steps):
            c.drawString(x+2, y+by-10, nums[i])

# ----- Document template -----
from reportlab.platypus.doctemplate import BaseDocTemplate, PageTemplate
from reportlab.platypus import Frame

class MyDocTemplate(BaseDocTemplate):
    pass

def add_header_footer(canvas_obj, doc):
    canvas_obj.saveState()
    w, h = A4
    
    # Header
    canvas_obj.setFillColor(BLUE_HEADER)
    canvas_obj.rect(0, h-28, w, 28, fill=1, stroke=0)
    canvas_obj.setFillColor(white)
    canvas_obj.setFont('YaHei', 10)
    canvas_obj.drawString(20, h-22, "\u6e56\u5357\u7701\u533b\u7597\u4fe1\u606f\u5b9e\u65f6\u67e5\u8be2\u5e73\u53f0")
    canvas_obj.setFont('YaHei', 8)
    canvas_obj.drawRightString(w-20, h-22, "\u5b9e\u65f6\u67e5\u8be2\u573a\u666f\u6280\u672f\u6587\u6863")
    
    canvas_obj.restoreState()

# Styles
S = {}
S['title'] = ParagraphStyle('title', fontName='YaHei-Bold', fontSize=16, 
    textColor=BLUE_TITLE, spaceAfter=4*mm, alignment=TA_CENTER)
S['h1'] = ParagraphStyle('h1', fontName='YaHei-Bold', fontSize=13, 
    textColor=BLUE_TITLE, spaceBefore=6*mm, spaceAfter=3*mm)
S['h2'] = ParagraphStyle('h2', fontName='YaHei-Bold', fontSize=11, 
    textColor=HexColor('#1a5276'), spaceBefore=4*mm, spaceAfter=2*mm)
S['h3'] = ParagraphStyle('h3', fontName='YaHei-Bold', fontSize=10, 
    textColor=HexColor('#2c7ba8'), spaceBefore=3*mm, spaceAfter=2*mm)
S['body'] = ParagraphStyle('body', fontName='YaHei', fontSize=9, 
    leading=14, spaceAfter=2*mm, alignment=TA_JUSTIFY)
S['bull'] = ParagraphStyle('bull', fontName='YaHei', fontSize=9, 
    leading=14, spaceAfter=1.5*mm, leftIndent=10)
S['th'] = ParagraphStyle('th', fontName='YaHei-Bold', fontSize=8.5, 
    textColor=white, alignment=TA_CENTER, leading=13)
S['td'] = ParagraphStyle('td', fontName='YaHei', fontSize=8, 
    leading=13, alignment=TA_CENTER)
S['note'] = ParagraphStyle('note', fontName='YaHei', fontSize=7.5,
    textColor=GRAY_TEXT, leading=12, spaceAfter=2*mm)
S['subtitle'] = ParagraphStyle('sub', fontName='YaHei-Bold', fontSize=22,
    textColor=BLUE_HEADER, spaceAfter=15*mm, alignment=TA_CENTER)
S['ver'] = ParagraphStyle('ver', fontName='YaHei', fontSize=11,
    textColor=GRAY_TEXT, spaceAfter=3*mm, alignment=TA_CENTER)
S['b'] = ParagraphStyle('b', fontName='YaHei-Bold', fontSize=9, spaceAfter=1*mm)
S['fc'] = ParagraphStyle('fc', fontName='YaHei-Bold', fontSize=9,
    textColor=BLUE_TITLE, spaceBefore=2*mm, spaceAfter=1*mm, alignment=TA_CENTER)

P = lambda s, st: Paragraph(s, S[st])

def make_table(headers, data, col_widths):
    hdr = [P(h, 'th') for h in headers]
    rows = [hdr]
    for row in data:
        rows.append([P(cell, 'td') for cell in row])
    t = Table(rows, colWidths=col_widths, repeatRows=1)
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), BLUE_TABLE_HEADER),
        ('TEXTCOLOR', (0, 0), (-1, 0), white),
        ('FONTNAME', (0, 0), (-1, -1), 'YaHei'),
        ('FONTSIZE', (0, 0), (-1, -1), 8),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor('#bdc3c7')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [white, HexColor('#f7f9fc')]),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
    ]))
    return t

# ----- Build -----
doc = MyDocTemplate(output_path, pagesize=A4, 
    leftMargin=20*mm, rightMargin=20*mm,
    topMargin=20*mm, bottomMargin=20*mm)

frame = Frame(doc.leftMargin, doc.bottomMargin, 
    doc.width, doc.height - 10*mm, id='normal')
doc.addPageTemplates([PageTemplate(id='main', frames=frame, onPage=add_header_footer)])

story = []

# ====== COVER ======
story.append(Spacer(1, 50*mm))
story.append(P("\u6e56\u5357\u7701\u533b\u7597\u4fe1\u606f\u5b9e\u65f6\u67e5\u8be2\u5e73\u53f0", 'title'))
story.append(P('<b>\u5b9e\u65f6\u67e5\u8be2\u573a\u666f\u6280\u672f\u6587\u6863</b>', 'subtitle'))
story.append(HRFlowable(width='60%', thickness=1, color=BLUE_HEADER, spaceAfter=15*mm, spaceBefore=5*mm))
story.append(P("\u7248\u672c\uff1av1.0", 'ver'))
story.append(P("\u65e5\u671f\uff1a2026-07-19", 'ver'))
story.append(P("\u7f16\u64b0\u5355\u4f4d\uff1a\u6e56\u5357\u4e50\u9014\u79d1\u6280\u6709\u9650\u516c\u53f8", 'ver'))

info = [["\u6587\u6863\u540d\u79f0", "\u5b9e\u65f6\u67e5\u8be2\u573a\u666f\u6280\u672f\u6587\u6863"],
        ["\u6240\u5c5e\u9879\u76ee", "\u6e56\u5357\u7701\u533b\u7597\u4fe1\u606f\u5b9e\u65f6\u67e5\u8be2\u5e73\u53f0"],
        ["\u6587\u6863\u7248\u672c", "v1.0"],
        ["\u521b\u5efa\u65e5\u671f", "2026-07-19"],
        ["\u7f16\u64b0\u5355\u4f4d", "\u6e56\u5357\u4e50\u9014\u79d1\u6280\u6709\u9650\u516c\u53f8"],
        ["\u5bc6\u7ea7", "\u5185\u90e8\u8d44\u6599"]]
it = Table(info, colWidths=[30*mm, 100*mm])
it.setStyle(TableStyle([
    ('FONTNAME', (0, 0), (-1, -1), 'YaHei'),
    ('FONTSIZE', (0, 0), (-1, -1), 9.5),
    ('TEXTCOLOR', (0, 0), (0, -1), BLUE_TITLE),
    ('TEXTCOLOR', (1, 0), (1, -1), GRAY_TEXT),
    ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
    ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ('TOPPADDING', (0, 0), (-1, -1), 5),
    ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ('LINEBELOW', (0, 0), (-1, -1), 0.3, HexColor('#e5e8e8')),
]))
story.append(it)
story.append(PageBreak())

# ====== Page 2: 项目目标 ======
story.append(P("\u4e00\u3001\u9879\u76ee\u76ee\u6807", 'h1'))
story.append(P("\uff08\u4e00\uff09\u80cc\u666f", 'h2'))
story.append(P("\u5065\u5eb7\u9669\u6838\u4fdd\u98ce\u63a7\u901a\u8fc7\u7cbe\u51c6\u98ce\u9669\u7b5b\u9009\u3001\u79d1\u5b66\u5b9a\u4ef7\u4e0e\u5408\u89c4\u7ecf\u8425\uff0c\u53ef\u6709\u6548\u63a7\u5236\u8fd0\u8425\u6210\u672c\u3001\u62d3\u5c55\u4fdd\u9669\u5e02\u573a\u3002\u7136\u800c\uff0c\u4f20\u7edf\u6a21\u5f0f\u4e0b\u4fdd\u9669\u516c\u53f8\u5728\u627f\u4fdd\u524d\u83b7\u53d6\u6295\u4fdd\u4eba\u533b\u7597\u5065\u5eb7\u6570\u636e\u5b58\u5728\u4ee5\u4e0b\u75db\u70b9\uff1a", 'body'))
story.append(P("\u2022 \u4fe1\u606f\u4e0d\u5bf9\u79f0\uff1a\u4fdd\u9669\u516c\u53f8\u96be\u4ee5\u5168\u9762\u83b7\u53d6\u53c2\u4fdd\u4eba\u7684\u771f\u5b9e\u5065\u5eb7\u72b6\u51b5\u53ca\u65e2\u5f80\u75c5\u53f2\uff0c\u6295\u4fdd\u4eba\u53ef\u80fd\u9690\u7792\u5173\u952e\u5065\u5eb7\u4fe1\u606f\uff0c\u52a0\u5267\u9006\u9009\u62e9\u98ce\u9669\u3002", 'bull'))
story.append(P("\u2022 \u6570\u636e\u6765\u6e90\u6709\u9650\uff1a\u4e3b\u8981\u4f9d\u8d56\u6295\u4fdd\u4eba\u81ea\u884c\u63d0\u4f9b\u7684\u4f53\u68c0\u62a5\u544a\u3001\u5065\u5eb7\u95ee\u5377\uff0c\u4fe1\u606f\u5e38\u4e0d\u5b8c\u6574\u3001\u4e0d\u51c6\u786e\u3001\u65f6\u6548\u6027\u5dee\u3002", 'bull'))
story.append(P("\u2022 \u67e5\u8be2\u6548\u7387\u4f4e\u4e0b\uff1a\u901a\u8fc7\u7ebf\u4e0b\u6e20\u9053\u5411\u533b\u7597\u673a\u6784\u9010\u4e00\u6838\u5b9e\uff0c\u6d41\u7a0b\u5197\u957f\u3001\u8017\u65f6\u8f83\u591a\uff0c\u5f71\u54cd\u5ba2\u6237\u6295\u4fdd\u4f53\u9a8c\u3002", 'bull'))
story.append(P("\u2022 \u6838\u4fdd\u6807\u51c6\u4e25\u683c\uff1a\u4e3a\u63a7\u5236\u98ce\u9669\uff0c\u4fdd\u9669\u516c\u53f8\u5e38\u8bbe\u5b9a\u8f83\u9ad8\u6838\u4fdd\u95e8\u69db\uff0c\u81f4\u4f7f\u90e8\u5206\u4eba\u7fa4\u88ab\u62d2\u4fdd\uff0c\u9650\u5236\u4fdd\u9669\u8986\u76d6\u9762\u3002", 'bull'))
story.append(P("\u57fa\u4e8e\u4e0a\u8ff0\u80cc\u666f\uff0c\u6e56\u5357\u7701\u533b\u7597\u4fe1\u606f\u5b9e\u65f6\u67e5\u8be2\u5e73\u53f0\u5b9a\u4f4d\u4e3a\u5168\u7701\uff15\uff11\u5bb6\u4fdd\u9669\u516c\u53f8\u7684\u6570\u636e\u4e2d\u53f0\uff0c\u5bf9\u63a5\u536b\u5065\u59d4\u3001\u533b\u4fdd\u5c40\u53ca\u6e56\u5357\u6570\u5b57\u4ea7\u4e1a\u96c6\u56e2\u7b49\u591a\u65b9\u6570\u636e\u6e90\uff0c\u63d0\u4f9b\u5b9e\u65f6\u3001\u5b89\u5168\u3001\u53ef\u8ba1\u8d39\u7684\u533b\u7597\u6570\u636e\u67e5\u8be2\u670d\u52a1\u3002", 'body'))

story.append(P("\uff08\u4e8c\uff09\u4ef7\u503c", 'h2'))
story.append(P('<b>\u793e\u4f1a\u6548\u76ca</b>', 'b'))
story.append(P("\u4f9d\u6258\u516c\u5171\u533b\u7597\u6570\u636e\u8d4b\u80fd\u5546\u4e1a\u5065\u5eb7\u9669\u6838\u4fdd\uff0c\u5168\u9762\u4f18\u5316\u6295\u4fdd\u5ba1\u6838\u6d41\u7a0b\u3002\u4ee5\u5b9e\u65f6\u6570\u636e\u67e5\u8be2\u53d6\u4ee3\u4f20\u7edf\u4f4e\u6548\u7684\u4eba\u5de5\u6838\u5b9e\uff0c\u663e\u8457\u63d0\u5347\u6838\u4fdd\u6548\u7387\uff0c\u6539\u5584\u7fa4\u4f17\u6295\u4fdd\u4f53\u9a8c\uff0c\u589e\u5f3a\u6c11\u4f17\u5065\u5eb7\u4fdd\u969c\u83b7\u5f97\u611f\u3002", 'body'))

story.append(P('<b>\u7ecf\u6d4e\u6548\u76ca</b>', 'b'))
story.append(P("\u5229\u7528\u516c\u5171\u6570\u636e\u6784\u5efa\u5b9e\u65f6\u67e5\u8be2\u901a\u9053\uff0c\u5b9e\u73b0\u6295\u4fdd\u4eba\u98ce\u9669\u7cbe\u51c6\u5206\u7ea7\u4e0e\u5dee\u5f02\u5316\u5b9a\u4ef7\uff0c\u4ece\u6e90\u5934\u9632\u8303\u9006\u9009\u62e9\u98ce\u9669\u4e0e\u627f\u4fdd\u98ce\u9669\u3002\u901a\u8fc7\u9ad8\u6548\u67e5\u8be2\u6d41\u7a0b\u964d\u4f4e\u4eba\u5de5\u6838\u4fdd\u6210\u672c\uff0c\u52a9\u529b\u4fdd\u9669\u516c\u53f8\u63a8\u51fa\u591a\u5143\u5316\u5065\u5eb7\u9669\u4ea7\u54c1\uff0c\u62d3\u5c55\u5e02\u573a\u7a7a\u95f4\u3002\u5e73\u53f0\u6309\u67e5\u8be2\u6b21\u6570\u6536\u8d39\uff0c\u5f62\u6210\u53ef\u6301\u7eed\u7684\u5546\u4e1a\u8fd0\u8425\u6a21\u5f0f\u3002", 'body'))

story.append(PageBreak())

# ====== Page 3: 数据需求 ======
story.append(P("\u4e8c\u3001\u4e3b\u8981\u5de5\u4f5c\u5185\u5bb9", 'h1'))
story.append(P("\uff08\u4e00\uff09\u6570\u636e\u9700\u6c42", 'h2'))
story.append(P("1\u3001\u9700\u6c42\u5b57\u6bb5", 'h3'))
story.append(P("\u5b9e\u65f6\u67e5\u8be2\u6db5\u76d6\u4ee5\u4e0b\u6570\u636e\u7ef4\u5ea6\uff0c\u6309\u5b57\u6bb5\u7528\u9014\u548c\u8131\u654f\u89c4\u5219\u5904\u7406\u540e\u8fd4\u56de\u7ed9\u4fdd\u9669\u516c\u53f8\uff1a", 'body'))

fd = [["\u6570\u636e\u57df", "\u6570\u636e\u5b57\u6bb5", "\u5b57\u6bb5\u7528\u9014", "\u8131\u654f\u89c4\u5219"],
      ["\u4eba\u5458\u57fa\u672c\u4fe1\u606f", "\u59d3\u540d", "\u4f5c\u4e3a\u67e5\u8be2\u6761\u4ef6\uff0c\u7528\u4e8e\u5b9a\u4f4d\u53c2\u4fdd\u4eba\u8eab\u4efd", "\u4fdd\u7559\u59d3\u6c0f\uff0c\u5176\u4f59\u7528*\u4ee3\u66ff"],
      ["\u4eba\u5458\u57fa\u672c\u4fe1\u606f", "\u8eab\u4efd\u8bc1\u53f7", "\u4f5c\u4e3a\u67e5\u8be2\u6761\u4ef6\u548c\u4e3b\u952e\uff0c\u5173\u8054\u591a\u8868\u6570\u636e", "\u4fdd\u7559\u524d4\u4f4d+\u540e4\u4f4d"],
      ["\u4eba\u5458\u57fa\u672c\u4fe1\u606f", "\u6027\u522b", "\u8f85\u52a9\u98ce\u9669\u8bc4\u4f30", "\u4fdd\u7559"],
      ["\u4eba\u5458\u57fa\u672c\u4fe1\u606f", "\u51fa\u751f\u65e5\u671f", "\u8ba1\u7b97\u5c31\u8bca\u65f6\u5e74\u9f84", "\u4fdd\u7559\u5e74\u4efd\u548c\u6708\u4efd"],
      ["\u4eba\u5458\u53c2\u4fdd\u4fe1\u606f", "\u9669\u79cd\u7c7b\u578b", "\u533a\u5206\u804c\u5de5/\u5c45\u6c11", "\u4fdd\u7559"],
      ["\u4eba\u5458\u53c2\u4fdd\u4fe1\u606f", "\u53c2\u4fdd\u72b6\u6001", "\u6821\u9a8c\u5f53\u524d\u662f\u5426\u6709\u6548\u53c2\u4fdd", "\u4fdd\u7559"],
      ["\u4eba\u5458\u53c2\u4fdd\u4fe1\u606f", "\u53c2\u4fdd\u65e5\u671f", "\u8ba1\u7b97\u8fde\u7eed\u53c2\u4fdd\u5e74\u9650", "\u4fdd\u7559\u5e74\u4efd"],
      ["\u5c31\u8bca\u4fe1\u606f", "\u5c31\u8bcaID", "\u6570\u636e\u5173\u8054\u6807\u8bc6", "\u63a9\u7801\u5904\u7406"],
      ["\u5c31\u8bca\u4fe1\u606f", "\u5b9a\u70b9\u533b\u7597\u673a\u6784\u540d\u79f0", "\u7528\u4e8e\u8bc4\u4f30\u5c31\u533b\u673a\u6784\u7b49\u7ea7", "\u4fdd\u7559"],
      ["\u5c31\u8bca\u4fe1\u606f", "\u533b\u9662\u7b49\u7ea7", "\u7528\u4e8e\u6838\u4fdd\u89c4\u5219\u5224\u65ad", "\u4fdd\u7559"],
      ["\u5c31\u8bca\u4fe1\u606f", "\u5165\u9662/\u51fa\u9662\u79d1\u5ba4", "\u7528\u4e8e\u75be\u75c5\u98ce\u9669\u8bc4\u4f30", "\u4fdd\u7559\u79d1\u5ba4\u5927\u7c7b"],
      ["\u5c31\u8bca\u4fe1\u606f", "\u8bca\u65ad\u4fe1\u606f", "\u7528\u4e8e\u65e2\u5f80\u75c7\u5224\u65ad", "\u6309\u5e73\u53f0\u89c4\u5219\u8131\u654f"],
      ["\u7ed3\u7b97\u4fe1\u606f", "\u7ed3\u7b97\u65f6\u95f4", "\u8ba1\u8d39\u65f6\u95f4", "\u4fdd\u7559\u65e5\u671f"],
      ["\u7ed3\u7b97\u4fe1\u606f", "\u533b\u7597\u7c7b\u522b", "\u4f4f\u9662/\u95e8\u8bca/\u7279\u75c5\u7b49\u5206\u7c7b", "\u4fdd\u7559"],
      ["\u7ed3\u7b97\u4fe1\u606f", "\u533b\u7597\u8d39\u603b\u989d", "\u7528\u4e8e\u8d39\u7528\u7c7b\u89c4\u5219\u5224\u65ad", "\u4fdd\u7559"],
      ["\u7ed3\u7b97\u4fe1\u606f", "\u7edf\u7b79\u57fa\u91d1\u652f\u51fa", "\u53cd\u6620\u533b\u4fdd\u62a5\u9500\u91d1\u989d", "\u4fdd\u7559"]]
ft = make_table(fd[0], fd[1:], [28*mm, 28*mm, 55*mm, 55*mm])
story.append(ft)
story.append(P("\u6ce8\uff1a\u59d3\u540d\u548c\u8eab\u4efd\u8bc1\u53f7\u7531\u4fdd\u9669\u516c\u53f8\u4f20\u5165\uff0c\u5176\u4f59\u5b57\u6bb5\u7531\u5e73\u53f0\u4ece\u6570\u636e\u6e90\u62c9\u53d6\u540e\u6309\u8131\u654f\u7b56\u7565\u8fd4\u56de\u3002", 'note'))

story.append(PageBreak())

# ====== Page 4: 流程架构图 ======
story.append(P("\uff08\u4e8c\uff09\u6d41\u7a0b\u67b6\u6784\u56fe", 'h2'))
story.append(P("\u5b9e\u65f6\u67e5\u8be2\u7684\u5b8c\u6574\u6d41\u7a0b\u5171\uff19\u4e2a\u6b65\u9aa4\uff0c\u6309\u6570\u636e\u6d41\u5411\u8fde\u63a5\u5982\u4e0b\uff1a", 'body'))

fc = FlowChart(A4[0] - 40*mm, 170)
story.append(Spacer(1, 3*mm))
story.append(fc)
story.append(Spacer(1, 3*mm))

sd = [["\u6b65\u9aa4", "\u53c2\u4e0e\u65b9", "\u64cd\u4f5c\u8bf4\u660e", "\u5173\u952e\u5904\u7406"],
      ["1", "\u4fdd\u9669\u516c\u53f8", "\u4f20\u5165\u59d3\u540d+\u8eab\u4efd\u8bc1\u53f7\uff0c\u53d1\u8d77\u67e5\u8be2\u8bf7\u6c42", "\u643a\u5e26AppKey"],
      ["2", "\u5e73\u53f0\u7f51\u5173", "AppKey\u8ba4\u8bc1\uff1a\u6821\u9a8cAppKey\u6709\u6548\u6027", "\u6821\u9a8cAppKey\u662f\u5426\u5b58\u5728\u4e14\u72b6\u6001\u6b63\u5e38"],
      ["3", "\u8ba1\u8d39\u5f15\u64ce", "\u67e5\u8be2\u524d\u9884\u7b97\u9884\u7559\uff1a\u68c0\u67e5\u516c\u53f8\u6708\u5ea6\u4f59\u989d", "\u4f59\u989d\u4e0d\u8db3\u5219\u8fd4\u56de4001\u9519\u8bef"],
      ["4", "\u6570\u636e\u8def\u7531", "\u6839\u636e\u67e5\u8be2\u7c7b\u578b\u8def\u7531\u81f3\u5bf9\u5e94\u6570\u636e\u6e90\u9002\u914d\u5668", "\u536b\u5065\u59d4/\u533b\u4fdd\u5c40/\u6570\u4ea7\u96c6\u56e2"],
      ["5", "\u5916\u90e8\u6570\u636e\u6e90", "\u8fd4\u56de\u539f\u59cb\u533b\u7597\u6570\u636e\uff08\u52a0\u5bc6\u4f20\u8f93\uff09", "\u5b89\u5168\u4f20\u8f93\u901a\u9053(TLS 1.3)"],
      ["6", "\u8131\u654f\u5f15\u64ce", "\u5bf9\u539f\u59cb\u6570\u636e\u8fdb\u884c\u8131\u654f\u5904\u7406", "\u59d3\u540d\u3001\u8eab\u4efd\u8bc1\u6309\u89c4\u5219\u8131\u654f"],
      ["7", "\u7ed3\u679c\u7ec4\u88c5", "\u7ec4\u88c5\u4e3a\u7edf\u4e00JSON\u683c\u5f0f\u8fd4\u56de", "\u533a\u5206HIT/NO_RESULT"],
      ["8", "\u8ba1\u8d39\u5f15\u64ce", "\u67e5\u8be2\u786e\u8ba4\u5165\u8d26\uff1a\u5199\u5165\u65e5\u5fd7\u548c\u8d39\u7528\u6d41\u6c34", "\u4e8b\u52a1\u4fdd\u8bc1\uff0c\u4e0d\u53ef\u62b5\u8d56"],
      ["9", "\u4fdd\u9669\u516c\u53f8", "\u63a5\u6536\u67e5\u8be2\u7ed3\u679c\uff0c\u7528\u4e8e\u6838\u4fdd\u51b3\u7b56", "\u2014"]]
st = make_table(sd[0], sd[1:], [10*mm, 22*mm, 60*mm, 74*mm])
story.append(st)
story.append(P("\u82e5\u67e5\u8be2\u8fc7\u7a0b\u4e2d\u4efb\u610f\u73af\u8282\u5931\u8d25\uff0c\u9884\u7559\u91d1\u989d\u81ea\u52a8\u56de\u6eda\uff0c\u4e0d\u4ea7\u751f\u5b9e\u9645\u8d39\u7528", 'note'))
story.append(P("\u5355\u6b21\u67e5\u8be2\u5e73\u5747\u54cd\u5e94\u65f6\u95f4\u76ee\u6807\uff1a<3\u79d2\uff08\u542b\u5916\u90e8\u6570\u636e\u6e90\u8c03\u7528\uff09", 'note'))

story.append(PageBreak())

# ====== Page 5: 技术方案 ======
story.append(P("\uff08\u4e09\uff09\u6280\u672f\u65b9\u6848", 'h2'))

story.append(P("1\u3001\u9274\u6743\u8ba4\u8bc1\u65b9\u6848", 'h3'))
story.append(P("\u4fdd\u9669\u516c\u53f8\u901a\u8fc7\u8fd0\u8425\u65b9\u7ebf\u4e0b\u5f00\u6237\u83b7\u53d6AppKey\u3002\u6bcf\u6b21API\u8bf7\u6c42\u5728\u8bf7\u6c42\u5934\u4e2d\u643a\u5e26AppKey\u8fdb\u884c\u8eab\u4efd\u8ba4\u8bc1\uff1a", 'body'))

ad = [["\u8bf7\u6c42\u5934", "\u5fc5\u586b", "\u8bf4\u660e"],
      ["X-App-Key", "\u662f", "\u8fd0\u8425\u65b9\u4ea4\u4ed8\u768432\u4f4dAppKey\uff0c\u7528\u4e8e\u6807\u8bc6\u4fdd\u9669\u516c\u53f8\u8eab\u4efd"]]
at = make_table(ad[0], ad[1:], [25*mm, 10*mm, 131*mm])
story.append(at)
story.append(P("\u5e73\u53f0\u6536\u5230\u8bf7\u6c42\u540e\uff0c\u6839\u636eAppKey\u67e5\u8be2\u4fdd\u9669\u516c\u53f8\u4fe1\u606f\uff0c\u6821\u9a8c\u8be5AppKey\u662f\u5426\u6709\u6548\u3001\u516c\u53f8\u72b6\u6001\u662f\u5426\u6b63\u5e38\u3002\u8ba4\u8bc1\u901a\u8fc7\u65b9\u53ef\u7ee7\u7eed\u67e5\u8be2\u6d41\u7a0b\u3002\u82e5AppKey\u65e0\u6548\u6216\u516c\u53f8\u5df2\u88ab\u505c\u7528\uff0c\u8fd4\u56de\u9519\u8bef\u7801403\u3002", 'body'))

story.append(P("2\u3001\u6570\u636e\u6e90\u9002\u914d\u4e0e\u8def\u7531\u65b9\u6848", 'h3'))
story.append(P("\u5e73\u53f0\u8bbe\u8ba1\u6570\u636e\u6e90\u9002\u914d\u5c42\uff08Data Adapter Layer\uff09\uff0c\u652f\u6301\u591a\u6570\u636e\u6e90\u63a5\u5165\uff1a", 'body'))

dsd = [["\u6570\u636e\u6e90", "\u5bf9\u63a5\u65b9\u5f0f", "\u6570\u636e\u7c7b\u578b", "\u72b6\u6001"],
       ["\u536b\u5065\u59d4\u63a5\u53e3", "HTTP RESTful API", "\u5c31\u8bca/\u7ed3\u7b97/\u8bca\u65ad\u6570\u636e", "\u5f85\u8054\u8c03"],
       ["\u533b\u4fdd\u5c40\u63a5\u53e3", "HTTP RESTful API", "\u53c2\u4fdd\u4fe1\u606f/\u8d39\u7528\u6570\u636e", "\u5f85\u8054\u8c03"],
       ["\u6e56\u5357\u6570\u5b57\u4ea7\u4e1a\u96c6\u56e2", "HTTP RESTful API / WebService", "\u7efc\u5408\u533b\u7597\u6570\u636e", "\u5f85\u8054\u8c03"],
       ["\u672c\u5730\u5386\u53f2\u6570\u636e\u5e93", "MySQL\u76f4\u8fde", "\u7f13\u5b58\u5386\u53f2\u67e5\u8be2\u6570\u636e(T+1)", "\u5df2\u5c31\u7eea"]]
dst = make_table(dsd[0], dsd[1:], [35*mm, 40*mm, 50*mm, 41*mm])
story.append(dst)
story.append(P("\u8def\u7531\u7b56\u7565\uff1a\u6839\u636equeryType\u53c2\u6570\u5c06\u8bf7\u6c42\u5206\u53d1\u81f3\u5bf9\u5e94\u7684\u6570\u636e\u6e90\u9002\u914d\u5668\u3002", 'body'))

story.append(P("3\u3001\u8ba1\u8d39\u6263\u8d39\u65b9\u6848", 'h3'))
story.append(P("\u7cfb\u7edf\u91c7\u7528\u6309\u6b21\u8ba1\u8d39\u3001\u5148\u9884\u7559\u540e\u786e\u8ba4\u7684\u8ba1\u8d39\u6a21\u578b\uff1a", 'body'))

bld = [["\u9636\u6bb5", "\u64cd\u4f5c", "\u8bf4\u660e"],
       ["\u67e5\u8be2\u524d", "\u9884\u7b97\u9884\u7559", "\u6309\u8be5\u516c\u53f8\u8be5\u63a5\u53e3\u7684\u8f83\u9ad8\u6807\u51c6\u4ece\u6708\u5ea6\u9884\u7b97\u4e2d\u9884\u7559\uff1b\u9884\u7b97\u4e0d\u8db3\u5219\u62d2\u7edd\u67e5\u8be2"],
       ["\u67e5\u8be2\u4e2d", "\u8c03\u7528\u5916\u90e8\u6570\u636e\u6e90", "\u67e5\u8be2\u5916\u90e8\u6570\u636e\u6e90\uff0c\u5224\u65ad\u662f\u5426\u67e5\u5f97\u7ed3\u679c"],
       ["\u67e5\u8be2\u540e", "\u786e\u8ba4\u5165\u8d26", "\u6839\u636e\u67e5\u5f97/\u672a\u67e5\u5f97\u7ed3\u679c\uff0c\u6309\u5bf9\u5e94\u4ef7\u683c\u786e\u8ba4\u6263\u8d39"],
       ["\u67e5\u8be2\u540e", "\u8bb0\u5f55\u65e5\u5fd7", "\u5199\u5165\u67e5\u8be2\u65e5\u5fd7\u8868\uff1a\u8d26\u5355\u6708\u4efd\u3001\u67e5\u8be2\u7c7b\u578b\u3001\u91d1\u989d\u5feb\u7167"],
       ["\u67e5\u8be2\u540e", "\u751f\u6210\u6d41\u6c34", "\u5199\u5165\u8d39\u7528\u6d41\u6c34\u8868\uff1a\u64cd\u4f5c\u7c7b\u578b\u3001\u524d\u540e\u4f59\u989d\u53d8\u66f4"]]
blt = make_table(bld[0], bld[1:], [15*mm, 22*mm, 129*mm])
story.append(blt)
story.append(P("\u9884\u7b97\u9884\u7559\u91c7\u7528\u6570\u636e\u5e93\u4e50\u89c2\u9501\u6216\u5206\u5e03\u5f0f\u9501\u4fdd\u8bc1\u5e76\u53d1\u5b89\u5168\uff0c\u9632\u6b62\u8d85\u6263", 'note'))
story.append(P("\u67e5\u8be2\u65e5\u5fd7\u5373\u8d26\u5355\u51ed\u8bc1\uff0c\u4e0d\u5141\u8bb8\u4fee\u6539\u6216\u5220\u9664\uff0c\u4fdd\u8bc1\u4e0d\u53ef\u62b5\u8d56", 'note'))
story.append(P("\u6708\u5ea6\u8d26\u5355\u6309\u81ea\u7136\u6708\u751f\u6210\uff0c\u533a\u5206\u67e5\u5f97/\u672a\u67e5\u5f97\u7ed3\u679c\uff0c\u6309\u63a5\u53e3\u7c7b\u578b\u6c47\u603b", 'note'))

story.append(PageBreak())

# ====== Page 6: 技术方案(续) + 接口协议开始 ======
story.append(P("4\u3001\u6570\u636e\u8131\u654f\u65b9\u6848", 'h3'))
story.append(P("\u8fd4\u56de\u7ed9\u4fdd\u9669\u516c\u53f8\u7684\u6570\u636e\u5fc5\u987b\u7ecf\u8fc7\u8131\u654f\u5904\u7406\uff1a", 'body'))

mkd = [["\u5b57\u6bb5", "\u8131\u654f\u65b9\u5f0f", "\u793a\u4f8b"],
       ["\u59d3\u540d", "\u4ec5\u4fdd\u7559\u59d3\u6c0f\uff0c\u5176\u4f59\u7528*\u4ee3\u66ff", "\u5f20*"],
       ["\u8eab\u4efd\u8bc1\u53f7", "\u4fdd\u7559\u524d4\u4f4d+\u540e4\u4f4d\uff0c\u4e2d\u95f4\u7528*\u4ee3\u66ff", "4301**********1234"],
       ["\u624b\u673a\u53f7", "\u4fdd\u7559\u524d3\u4f4d+\u540e4\u4f4d", "139****5678"],
       ["\u8bca\u65ad\u4fe1\u606f", "\u6309\u5e73\u53f0\u89c4\u5219\u8131\u654f", "\u8bca\u65ad\u6458\u8981"],
       ["\u8d39\u7528\u660e\u7ec6", "\u4fdd\u7559\u603b\u989d\uff0c\u4e0d\u5c55\u793a\u5185\u90e8\u660e\u7ec6\u9879", "\u603b\u91d1\u989d: 3500.00\u5143"],
       ["\u5c31\u8bcaID/\u7ed3\u7b97ID", "\u63a9\u7801\u5904\u7406", "****-****-****"]]
mkt = make_table(mkd[0], mkd[1:], [30*mm, 50*mm, 86*mm])
story.append(mkt)
story.append(P("\u8131\u654f\u5728\u6570\u636e\u6e90\u8fd4\u56de\u539f\u59cb\u6570\u636e\u540e\u5728\u670d\u52a1\u7aef\u5185\u5b58\u4e2d\u5b8c\u6210\uff0c\u539f\u59cb\u660e\u6587\u4e0d\u843d\u76d8\u5b58\u50a8\u3002", 'body'))

story.append(P("5\u3001\u67e5\u8be2\u65e5\u5fd7\u4e0e\u5bf9\u8d26\u65b9\u6848", 'h3'))
story.append(P("\u6bcf\u6b21\u67e5\u8be2\u5fc5\u987b\u8bb0\u5f55\u65e5\u5fd7\uff0c\u65e5\u5fd7\u5373\u8ba1\u8d39\u51ed\u8bc1\u3002\u65e5\u5fd7\u8868\u6838\u5fc3\u5b57\u6bb5\u5982\u4e0b\uff1a", 'body'))

lgd = [["\u5b57\u6bb5", "\u8bf4\u660e"],
       ["\u4fdd\u9669\u516c\u53f8ID", "\u5173\u8054\u4fdd\u9669\u516c\u53f8\u4e3b\u4f53"],
       ["\u67e5\u8be2\u65f6\u95f4", "\u7cbe\u786e\u5230\u6beb\u79d2\u7684\u65f6\u95f4\u6233"],
       ["\u67e5\u8be2\u7c7b\u578b", "\u533b\u7597\u5927\u6570\u636e\u67e5\u8be2\u3001\u533b\u4fdd\u4fe1\u606f\u67e5\u8be2\u7b49"],
       ["\u67e5\u8be2\u53c2\u6570", "\u59d3\u540d\uff08\u8131\u654f\uff09\u3001\u8eab\u4efd\u8bc1\u53f7\uff08\u8131\u654f\uff09"],
       ["\u8d26\u5355\u6708\u4efd", "\u6240\u5c5e\u81ea\u7136\u6708\uff08yyyy-MM\uff09"],
       ["\u7ed3\u679c\u7c7b\u578b", "HIT/NO_RESULT"],
       ["\u6263\u8d39\u91d1\u989d", "\u672c\u6b21\u67e5\u8be2\u5b9e\u9645\u6263\u8d39\u91d1\u989d"],
       ["\u9884\u7559\u91d1\u989d", "\u67e5\u8be2\u524d\u9884\u7559\u7684\u9884\u7b97\u91d1\u989d"]]
lgt = make_table(lgd[0], lgd[1:], [25*mm, 141*mm])
story.append(lgt)
story.append(P("\u6bcf\u67081\u65e5\u81ea\u52a8\u751f\u6210\u4e0a\u6708\u7684\u5bf9\u8d26\u8d26\u5355\uff0c\u6309\u63a5\u53e3\u7c7b\u578b\u548c\u67e5\u5f97/\u672a\u67e5\u5f97\u7ed3\u679c\u6c47\u603b\u6b21\u6570\u4e0e\u91d1\u989d\u3002", 'body'))

story.append(PageBreak())

# ====== Page 7: 接口协议 ======
story.append(P("\uff08\u56db\uff09\u63a5\u53e3\u534f\u8bae", 'h2'))

story.append(P("\u4ea7\u54c1\u8f93\u5165", 'h3'))
story.append(P("\u5b9e\u65f6\u67e5\u8be2\u7edf\u4e00\u5165\u53e3\u63a5\u53e3\uff1a", 'body'))

ipd = [["\u8f93\u5165\u53c2\u6570", "\u7c7b\u578b", "\u5fc5\u586b", "\u8bf4\u660e", "\u793a\u4f8b"],
       ["queryType", "String", "\u662f", "\u67e5\u8be2\u7c7b\u578b\u6807\u8bc6", "medical_all"],
       ["name", "String", "\u662f", "\u88ab\u67e5\u8be2\u4eba\u59d3\u540d\uff08UTF-8\u7f16\u7801\uff09", "\u5f20\u4e09"],
       ["idCard", "String", "\u662f", "\u88ab\u67e5\u8be2\u4eba\u8eab\u4efd\u8bc1\u53f7", "430102199001011234"],
       ["queryStartDate", "String", "\u5426", "\u67e5\u8be2\u8d77\u59cb\u65e5\u671f", "2026-01-01"],
       ["queryEndDate", "String", "\u5426", "\u67e5\u8be2\u7ed3\u675f\u65e5\u671f", "2026-12-31"]]
ipt = make_table(ipd[0], ipd[1:], [22*mm, 13*mm, 8*mm, 60*mm, 63*mm])
story.append(ipt)
story.append(P("\u8bf7\u6c42\u5934\u643a\u5e26 X-App-Key \u8fdb\u884c\u8eab\u4efd\u8ba4\u8bc1\u3002", 'note'))

story.append(P("\u4ea7\u54c1\u8f93\u51fa", 'h3'))
story.append(P("\u67e5\u8be2\u7ed3\u679cJSON\u7ed3\u6784\u5982\u4e0b\uff1a", 'body'))

opd = [["\u8f93\u51fa\u53c2\u6570", "\u7c7b\u578b", "\u8bf4\u660e", "\u793a\u4f8b"],
       ["queryType", "String", "\u67e5\u8be2\u7c7b\u578b\u6807\u8bc6", "medical_all"],
       ["queryName", "String", "\u67e5\u8be2\u7c7b\u578b\u540d\u79f0", "\u533b\u7597\u5927\u6570\u636e"],
       ["fee", "Decimal", "\u672c\u6b21\u67e5\u8be2\u91d1\u989d", "10.00"],
       ["name", "String", "\u8131\u654f\u540e\u59d3\u540d", "\u5f20*"],
       ["idCard", "String", "\u8131\u654f\u540e\u8eab\u4efd\u8bc1\u53f7", "4301**********1234"],
       ["queryTime", "String", "\u67e5\u8be2\u65f6\u95f4\uff08ISO8601\uff09", "2026-07-19T10:30:00+08:00"],
       ["resultStatus", "String", "\u67e5\u8be2\u7ed3\u679c\u72b6\u6001", "HIT / NO_RESULT"],
       ["summary", "String", "\u67e5\u8be2\u7ed3\u679c\u6458\u8981", "\u5171\u67e5\u5f973\u6761\u5c31\u8bca\u8bb0\u5f55"],
       ["records", "Array", "\u5177\u4f53\u6570\u636e\u8bb0\u5f55\u5217\u8868\uff08\u8131\u654f\u540e\uff09", "[...]"]]
opt = make_table(opd[0], opd[1:], [25*mm, 15*mm, 60*mm, 66*mm])
story.append(opt)

story.append(P("\u9519\u8bef\u7801\u8bf4\u660e", 'h3'))

erd = [["\u9519\u8bef\u7801", "HTTP\u72b6\u6001\u7801", "\u8bf4\u660e", "\u6392\u67e5\u65b9\u5411"],
       ["400", "400", "\u8bf7\u6c42\u53c2\u6570\u4e0d\u5b8c\u6574", "\u68c0\u67e5queryType\u3001name\u3001idCard\u5fc5\u586b\u5b57\u6bb5"],
       ["403", "403", "AppKey\u65e0\u6548\u6216\u4fdd\u9669\u516c\u53f8\u5df2\u505c\u7528", "\u8054\u7cfb\u8fd0\u8425\u65b9\u786e\u8ba4AppKey\u548c\u516c\u53f8\u72b6\u6001"],
       ["402", "402", "\u6708\u5ea6\u9884\u7b97\u4e0d\u8db3", "\u8054\u7cfb\u8fd0\u8425\u65b9\u8c03\u6574\u6708\u5ea6\u9884\u7b97"],
       ["500", "500", "\u670d\u52a1\u5668\u5185\u90e8\u9519\u8bef", "\u8054\u7cfb\u5e73\u53f0\u6280\u672f\u652f\u6301"]]
ert = make_table(erd[0], erd[1:], [15*mm, 22*mm, 55*mm, 74*mm])
story.append(ert)

# Build
doc.build(story)
print('PDF generated successfully: ' + output_path)
print(f'File size: {os.path.getsize(output_path)} bytes')
