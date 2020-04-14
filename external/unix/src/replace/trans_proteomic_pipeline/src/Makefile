###################################################################################
# Program       : Makefile                                                        #
# Author        : Andrew Keller <akeller@systemsbiology.org>                      #
# Date          : 11.27.02                                                        #
#                                                                                 #
# for compilation of C++ version of TPP, including PeptideProphet                 #
#                                                                                 #
# Copyright (C) 2003 Andrew Keller                                                #
#                                                                                 #
# This library is free software; you can redistribute it and/or                   #
# modify it under the terms of the GNU Lesser General Public                      #
# License as published by the Free Software Foundation; either                    #
# version 2.1 of the License, or (at your option) any later version.              #
#                                                                                 #
# This library is distributed in the hope that it will be useful,                 #
# but WITHOUT ANY WARRANTY; without even the implied warranty of                  #
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU               #
# General Public License for more details.                                        #
#                                                                                 #
# You should have received a copy of the GNU Lesser General Public                #
# License along with this library; if not, write to the Free Software             #
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA       #
#                                                                                 #
# Andrew Keller                                                                   #
# Insitute for Systems Biology                                                    #
# 1441 North 34th St.                                                             #
# Seattle, WA  98103  USA                                                         #
# akeller@systemsbiology.org                                                      #
#                                                                                 #
###################################################################################

#
# A note about TPP Version and Build Numbers
#
# The file src/common/TPPVersion.h controls the version info displayed by TPP components.
# This info is furnished to the various exe and cgi files by linking with an object file
# that also contains a build number (timestamp, actually).  The source code for this object 
# file is generated automatically, see target TPPVersionInfo.cxx: below.  For perl scripts
# the info is generated as an include file TPPVersionInfo.pl.
#
# The build number (timestamp) is refreshed when src/common/TPPVersion.h is changed,
# or during a "make clean all".  The intent is that the build number be the same across
# all TPP components delivered to the end user.  It could of course be refreshed with 
# every "make all" but that would mean every app would relink every time, which would bog 
# down the development cycle.  If you wish to force a build number update without doing 
# a "make clean", use "make new_buildnum".
#
#

# Include configuration variables for the build
include Makefile.incl

EXPAT = ../extern/expat-2.0.0
# yes, the headers are in the 'lib' directory
EXPAT_INCL = -I$(EXPAT)/lib

LIBARCHIVE = ../extern/libarchive-2.2.7

FASTAR = ../extern/fastar

ifeq (${LGPL},1) 
INCLUDE=-I. \
  -I$(FASTAR)/spare \
  -I$(FASTAR)/common \
  -IVisualization \
  -IVisualization/Pep3D \
  -IVisualization/Pep3D/cgi_Pep3D_xml \
  -IVisualization/xinteract \
  -IVisualization/InteractParser \
  -IVisualization/Comet \
  -IVisualization/Comet/sequest-tgz-out \
  -IVisualization/Comet/plot-msms \
  -IVisualization/Comet/comet-fastadb \
  -IVisualization/SpectraST \
  -IValidation \
  -IValidation/PeptideProphet \
  -IValidation/ProteinProphet \
  -IValidation/PeptideProphet/PeptideProphetParser \
  -IValidation/PeptideProphet/PeptideProphetOptions \
  -IValidation/ModelParser \
  -IValidation/MixtureModel \
  -IValidation/MixtureDistribution \
  -IValidation/Distribution \
  -IValidation/DiscriminateFunction \
  -IValidation/DiscriminateFunction/Sequest \
  -IValidation/DiscriminateFunction/ProbID \
  -IValidation/DiscriminateFunction/Mascot \
  -IValidation/DiscriminateFunction/Comet \
  -IValidation/DiscriminateFunction/Tandem \
  -IValidation/DiscriminateFunction/Phenyx \
  -IValidation/DiscriminateFunction/SpectraST \
  -Irunperl_src \
  -IQuantitation \
  -IQuantitation/XPress \
  -IQuantitation/XPress/XPressProteinRatioParser \
  -IQuantitation/XPress/XPressPeptideUpdateParser \
  -IQuantitation/XPress/XPressPeptideParser \
  -IQuantitation/XPress/XPressCGIProteinDisplayParser \
  -IQuantitation/XPress/XPressCGIParser \
  -IQuantitation/RatioParser \
  -IQuantitation/Libra \
  -IQuantitation/Libra/LibraProteinRatioParser \
  -IQuantitation/Libra/LibraPeptideParser \
  -IQuantitation/ASAPRatio \
  -IQuantitation/ASAPRatio/UniquePeptide \
  -IQuantitation/ASAPRatio/PvalueModel \
  -IQuantitation/ASAPRatio/Normalization \
  -IQuantitation/ASAPRatio/ASAPRatio_Fns \
  -IQuantitation/ASAPRatio/ASAPRatioPvalueParser \
  -IQuantitation/ASAPRatio/ASAPRatioProteinRatioParser \
  -IQuantitation/ASAPRatio/ASAPRatioPeptideParser \
  -IQuantitation/ASAPRatio/ASAPRatioPeptideCGIDisplayParser \
  -IQuantitation/ASAPRatio/ASAPCGIDisplay \
  -IQuantitation/Q3/Q3ProteinRatioParser \
  -IParsers \
  -IParsers/RefreshParser \
  -IParsers/MzXML2Search\
  -IParsers/ramp \
  -IParsers/Parser \
  -IParsers/DatabaseParser \
  -IParsers/CompactParser \
  -IParsers/AnalysisSummaryParser \
  -IParsers/Algorithm2XML \
  -IParsers/Algorithm2XML/Sequest2XML \
  -IParsers/Algorithm2XML/Sqt2XML \
  -IParsers/Algorithm2XML/SearchResult \
  -IParsers/Algorithm2XML/SearchParams \
  -IParsers/Algorithm2XML/Mascot2XML \
  -IParsers/Algorithm2XML/Comet2XML \
  -IParsers/Algorithm2XML/Tandem2XML \
  -Imisc \
  -IEnzyme \
  -IEnzyme/ProteolyticEnzyme \
  -IEnzyme/ProteolyticEnzyme/ProteolyticEnzymeFactory \
  -IEnzyme/EnzymeDigestionParser \
  -IEnzyme/EnzymeDigestion \
  -Icommon \
  -Icommon/ResidueMass \
  -Icommon/ModifiedResidueMass \
  -Icommon/ModificationInfo \
  -ISearch/SpectraST \
  -I$(LIBARCHIVE)/libarchive \
  -I../../../deploy/include

else 
INCLUDE=-I. \
  -Irefresh_interact \
  -IVisualization \
  -IVisualization/Pep3D \
  -IVisualization/Pep3D/cgi_Pep3D_xml \
  -IVisualization/xinteract \
  -IVisualization/InteractParser \
  -IVisualization/Comet \
  -IVisualization/Comet/sequest-tgz-out \
  -IVisualization/Comet/plot-msms \
  -IVisualization/Comet/comet-fastadb \
  -IVisualization/SpectraST \
  -IValidation \
  -IValidation/ProteinProphet \
  -IValidation/PeptideProphet \
  -IValidation/PeptideProphet/PeptideProphetParser \
  -IValidation/PeptideProphet/PeptideProphetOptions \
  -IValidation/ModelParser \
  -IValidation/MixtureModel \
  -IValidation/MixtureDistribution \
  -IValidation/Distribution \
  -IValidation/DiscriminateFunction \
  -IValidation/DiscriminateFunction/Sequest \
  -IValidation/DiscriminateFunction/ProbID \
  -IValidation/DiscriminateFunction/Mascot \
  -IValidation/DiscriminateFunction/Comet \
  -IValidation/DiscriminateFunction/Tandem \
  -IValidation/DiscriminateFunction/Phenyx \
  -IValidation/DiscriminateFunction/SpectraST \
  -Irunperl_src \
  -IQuantitation \
  -IQuantitation/XPress \
  -IQuantitation/XPress/XPressProteinRatioParser \
  -IQuantitation/XPress/XPressPeptideUpdateParser \
  -IQuantitation/XPress/XPressPeptideParser \
  -IQuantitation/XPress/XPressCGIProteinDisplayParser \
  -IQuantitation/XPress/XPressCGIParser \
  -IQuantitation/RatioParser \
  -IQuantitation/Libra \
  -IQuantitation/Libra/LibraProteinRatioParser \
  -IQuantitation/Libra/LibraPeptideParser \
  -IQuantitation/ASAPRatio \
  -IQuantitation/ASAPRatio/UniquePeptide \
  -IQuantitation/ASAPRatio/PvalueModel \
  -IQuantitation/ASAPRatio/Normalization \
  -IQuantitation/ASAPRatio/ASAPRatio_Fns \
  -IQuantitation/ASAPRatio/ASAPRatioPvalueParser \
  -IQuantitation/ASAPRatio/ASAPRatioProteinRatioParser \
  -IQuantitation/ASAPRatio/ASAPRatioPeptideParser \
  -IQuantitation/ASAPRatio/ASAPRatioPeptideCGIDisplayParser \
  -IQuantitation/ASAPRatio/ASAPCGIDisplay \
  -IParsers \
  -IParsers/RefreshParser \
  -IParsers/MzXML2Search\
  -IParsers/ramp \
  -IParsers/Parser \
  -IParsers/DatabaseParser \
  -IParsers/CompactParser \
  -IParsers/AnalysisSummaryParser \
  -IParsers/Algorithm2XML \
  -IParsers/Algorithm2XML/Sequest2XML \
  -IParsers/Algorithm2XML/Sqt2XML \
  -IParsers/Algorithm2XML/SearchResult \
  -IParsers/Algorithm2XML/SearchParams \
  -IParsers/Algorithm2XML/Mascot2XML \
  -IParsers/Algorithm2XML/Comet2XML \
  -IParsers/Algorithm2XML/Tandem2XML \
  -Imisc \
  -IEnzyme \
  -IEnzyme/ProteolyticEnzyme \
  -IEnzyme/ProteolyticEnzyme/ProteolyticEnzymeFactory \
  -IEnzyme/EnzymeDigestionParser \
  -IEnzyme/EnzymeDigestion \
  -Icommon \
  -Icommon/ResidueMass \
  -Icommon/ModifiedResidueMass \
  -Icommon/ModificationInfo \
  -ISearch/SpectraST \
  -I$(LIBARCHIVE)/libarchive \
  -I../../../deploy/include
endif


INCLUDE+= $(EXPAT_INCL)

ifeq (${XML_ONLY},1)
XMLFLAGS= -DXML_ONLY
else
XMLFLAGS=
endif

ifeq (${LGPL},1)
LGPLFLAG= -D__LGPL__
else
LGPLFLAG=
endif

# on linux, assume libboost_regex-gcc-mt.a, mac is libboost_regex.a
ifeq (${OSTYPE},linux)
BOOST_REGEX_FILE=libboost_regex-gcc-mt.a
else
BOOST_REGEX_FILE=libboost_regex.a
endif

#
# cygwin or linux?
#
ifeq (${OS},Windows_NT)
OSFLAGS= -D__CYGWIN__
GD_LIB= /lib/libgd.a
BOOST_REGEX_LIB=  /lib/libboost_regex-gcc-mt.a
else
OSFLAGS= -D__LINUX__
GD_LIB= -lgd
BOOST_REGEX_LIB= ../../../deploy/lib/${BOOST_REGEX_FILE} -lpthread
endif

CFLAGS= ${OSFLAGS} ${XMLFLAGS} ${LGPLFLAG} -D__INTEL__ -DINLINING

#DEBUG= -g

# put here the name of c++ compiler
CC=g++ ${CFLAGS} ${DEBUG} ${INCLUDE} -Dlint -pedantic-errors -Wformat -Wstrict-aliasing  -Wno-deprecated -Wno-char-subscripts -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE


# put here the complete directory path for the include subdirectory
CCINC= -I../../../deploy/include -I../../../deploy/include/boost-1_33_1

# here again the name of the c++ compiler
#LD=g++ -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE
LD=g++ ${INCLUDE} -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -L../../../deploy/lib

# here the complete directory path for the lib subdirectory followed by  -lm
LDFLAGS= -L/usr/local/lib -L/usr/lib -L../../../deploy/lib -Lboost_regex -lboost_filesystem -lboost_serialization -lm ${DEBUG}
OLD = 
#-oldcxx

######################################################################
#
# do not edit past this point
#
######################################################################

#
# rules
#

.SUFFIXES:	.o .cxx .c .cpp

.cxx.o:	
		$(CC) $(OLD) $(DEBUG) $(CCOPT) $(CCINC)  -c -O2 $<

.c.o:
		$(CC) $(OLD) $(DEBUG) $(CCOPT) $(CCINC)  -c -O2 $<

.cpp.o:
		$(CC) $(OLD) $(DEBUG) $(CCOPT) $(CCINC)  -c -O2 $<
ifeq (${LGPL},1) 
VPATH = common \
  common/ModificationInfo \
  common/ModifiedResidueMass \
  common/ResidueMass \
  Enzyme \
  Enzyme/EnzymeDigestion \
  Enzyme/EnzymeDigestionParser \
  Enzyme/ProteolyticEnzyme \
  Enzyme/ProteolyticEnzyme/ProteolyticEnzymeFactory \
  misc \
  misc/out2summary \
  Parsers \
  Parsers/Algorithm2XML \
  Parsers/Algorithm2XML/Tandem2XML \
  Parsers/Algorithm2XML/Comet2XML \
  Parsers/Algorithm2XML/Mascot2XML \
  Parsers/Algorithm2XML/SearchParams \
  Parsers/Algorithm2XML/SearchResult \
  Parsers/Algorithm2XML/Sequest2XML \
  Parsers/Algorithm2XML/Sqt2XML \
  Parsers/AnalysisSummaryParser \
  Parsers/CompactParser \
  Parsers/DatabaseParser \
  Parsers/Parser \
  Parsers/ramp \
  Parsers/RefreshParser \
  Parsers/MzXML2Search\
  regenerate_VersionInfo \
  Quantitation \
  Quantitation/ASAPRatio \
  Quantitation/ASAPRatio/ASAPCGIDisplay \
  Quantitation/ASAPRatio/ASAPRatio_Fns \
  Quantitation/ASAPRatio/ASAPRatioPeptideCGIDisplayParser \
  Quantitation/ASAPRatio/ASAPRatioPeptideParser \
  Quantitation/ASAPRatio/ASAPRatioProteinRatioParser \
  Quantitation/ASAPRatio/ASAPRatioPvalueParser \
  Quantitation/ASAPRatio/Normalization \
  Quantitation/ASAPRatio/PvalueModel \
  Quantitation/ASAPRatio/UniquePeptide \
  Quantitation/Libra \
  Quantitation/Libra/LibraPeptideParser \
  Quantitation/Libra/LibraProteinRatioParser \
  Quantitation/RatioParser \
  Quantitation/XPress \
  Quantitation/XPress/XPressCGIParser \
  Quantitation/XPress/XPressCGIProteinDisplayParser \
  Quantitation/XPress/XPressPeptideParser \
  Quantitation/XPress/XPressPeptideUpdateParser \
  Quantitation/XPress/XPressProteinRatioParser \
  runperl_src \
  Validation \
  Validation/DiscriminateFunction \
  Validation/DiscriminateFunction/Comet \
  Validation/DiscriminateFunction/Mascot \
  Validation/DiscriminateFunction/ProbID \
  Validation/DiscriminateFunction/Sequest \
  Validation/DiscriminateFunction/Tandem \
  Validation/DiscriminateFunction/Phenyx \
  Validation/DiscriminateFunction/SpectraST \
  Validation/Distribution \
  Validation/MixtureDistribution \
  Validation/MixtureModel \
  Validation/ModelParser \
  Validation/ProteinProphet \
  Validation/PeptideProphet \
  Validation/PeptideProphet/PeptideProphetOptions \
  Validation/PeptideProphet/PeptideProphetParser \
  Visualization \
  Visualization/Pep3D \
  Visualization/Pep3D/cgi_Pep3D_xml \
  Visualization/Comet \
  Visualization/Comet/comet-fastadb \
  Visualization/Comet/plot-msms \
  Visualization/Comet/sequest-tgz-out \
  Visualization/InteractParser \
  Visualization/xinteract \
  Visualization/SpectraST \
  Search/SpectraST \
  $(FASTAR)/spare \
  $(FASTAR)/common 
else 
VPATH = common \
  common/ModificationInfo \
  common/ModifiedResidueMass \
  common/ResidueMass \
  Enzyme \
  Enzyme/EnzymeDigestion \
  Enzyme/EnzymeDigestionParser \
  Enzyme/ProteolyticEnzyme \
  Enzyme/ProteolyticEnzyme/ProteolyticEnzymeFactory \
  misc \
  misc/out2summary \
  Parsers \
  Parsers/Algorithm2XML \
  Parsers/Algorithm2XML/Tandem2XML \
  Parsers/Algorithm2XML/Comet2XML \
  Parsers/Algorithm2XML/Mascot2XML \
  Parsers/Algorithm2XML/SearchParams \
  Parsers/Algorithm2XML/SearchResult \
  Parsers/Algorithm2XML/Sequest2XML \
  Parsers/Algorithm2XML/Sqt2XML \
  Parsers/AnalysisSummaryParser \
  Parsers/CompactParser \
  Parsers/DatabaseParser \
  Parsers/Parser \
  Parsers/ramp \
  Parsers/RefreshParser \
  Parsers/MzXML2Search\
  regenerate_VersionInfo \
  Quantitation \
  Quantitation/ASAPRatio \
  Quantitation/ASAPRatio/ASAPCGIDisplay \
  Quantitation/ASAPRatio/ASAPRatio_Fns \
  Quantitation/ASAPRatio/ASAPRatioPeptideCGIDisplayParser \
  Quantitation/ASAPRatio/ASAPRatioPeptideParser \
  Quantitation/ASAPRatio/ASAPRatioProteinRatioParser \
  Quantitation/ASAPRatio/ASAPRatioPvalueParser \
  Quantitation/ASAPRatio/Normalization \
  Quantitation/ASAPRatio/PvalueModel \
  Quantitation/ASAPRatio/UniquePeptide \
  Quantitation/Libra \
  Quantitation/Libra/LibraPeptideParser \
  Quantitation/Libra/LibraProteinRatioParser \
  Quantitation/RatioParser \
  Quantitation/XPress \
  Quantitation/XPress/XPressCGIParser \
  Quantitation/XPress/XPressCGIProteinDisplayParser \
  Quantitation/XPress/XPressPeptideParser \
  Quantitation/XPress/XPressPeptideUpdateParser \
  Quantitation/XPress/XPressProteinRatioParser \
  Quantitation/Q3/Q3ProteinRatioParser \
  runperl_src \
  Validation \
  Validation/DiscriminateFunction \
  Validation/DiscriminateFunction/Comet \
  Validation/DiscriminateFunction/Mascot \
  Validation/DiscriminateFunction/ProbID \
  Validation/DiscriminateFunction/Sequest \
  Validation/DiscriminateFunction/Tandem \
  Validation/DiscriminateFunction/Phenyx \
  Validation/DiscriminateFunction/SpectraST \
  Validation/Distribution \
  Validation/MixtureDistribution \
  Validation/MixtureModel \
  Validation/ModelParser \
  Validation/ProteinProphet \
  Validation/PeptideProphet \
  Validation/PeptideProphet/PeptideProphetOptions \
  Validation/PeptideProphet/PeptideProphetParser \
  Visualization \
  Visualization/Pep3D \
  Visualization/Pep3D/cgi_Pep3D_xml \
  Visualization/Comet \
  Visualization/Comet/comet-fastadb \
  Visualization/Comet/plot-msms \
  Visualization/Comet/sequest-tgz-out \
  Visualization/InteractParser \
  Visualization/xinteract \
  Visualization/SpectraST \
  Search/SpectraST \
  refresh_interact
endif	


#
# targets
#

EXE_XML = regenerate_VersionInfo/regenerate_VersionInfo PeptideProphetParser ProteinProphet EnzymeDigestionParser XPressPeptideParser ASAPRatioPeptideParser ASAPRatioProteinRatioParser InteractParser DatabaseParser RefreshParser MzXML2Search XPressProteinRatioParser Q3ProteinRatioParser ASAPRatioPvalueParser CompactParser xinteract runperl Sequest2XML Mascot2XML Comet2XML Tandem2XML LibraPeptideParser LibraProteinRatioParser spectrast Out2XML Sqt2XML CombineOut plotspectrast \
dtafilter runsearch out2summary

EXE_HTML = ASAPRatioPeptideCGIDisplayParser.cgi ASAPCGIDisplay.cgi ModelParser.cgi XPressPeptideUpdateParser.cgi XPressCGIProteinDisplayParser.cgi Pep3D_xml.cgi disCidData_xml.cgi getCidData_xml.cgi AnalysisSummaryParser.cgi comet-fastadb.cgi plot-msms.cgi sequest-tgz-out.cgi xpress-prophet-update.cgi cometresult.cgi PepXMLViewer.cgi plotspectrast.cgi dta-xml.cgi

ifeq (${XML_ONLY},1)
EXE = ${EXE_XML}
else
EXE = ${EXE_XML} ${EXE_HTML}
endif

all :  ${EXE}
	make -C ../COVERAGE/

#
# stuff for getting build numbers into the various apps and CGIs
#

regenerate_VersionInfo/regenerate_VersionInfo: regenerate_VersionInfo.o
	$(LD) $(DEBUG) regenerate_VersionInfo.o -O2 -o regenerate_VersionInfo/regenerate_VersionInfo

TPPVersionInfo.cxx : TPPVersion.h regenerate_VersionInfo/regenerate_VersionInfo
	regenerate_VersionInfo/regenerate_VersionInfo
	$(CC) $(OLD) $(DEBUG) $(CCOPT) $(CCINC)  -c -O2 TPPVersionInfo.cxx
	cp TPPVersionInfo.pl ../perl
	cp TPPVersionInfo.pl ../CGI

new_buildnum :
	rm TPPVersionInfo.cxx
	make all

#
# cygwin or linux?
#
ifeq (${OS},Windows_NT)

install: install-windows

configure:

else

install: install-linux

configure: configure-linux

endif

#
# put common stuff in an archive for ease of makefile maintenance
#
TPPLIB_OBJS = MixtureModel.o Tag.o TagListComparator.o MixtureDistr.o DiscrimValMixtureDistr.o DiscreteMixtureDistr.o NTTMixtureDistr.o NMCMixtureDistr.o ICATMixtureDistr.o DiscriminantFunction.o SequestDiscrimFunction.o AbbrevSequestDiscrimFunction.o ToftofAbbrevSequestDiscrimFunction.o Distribution.o DiscreteDistribution.o NonParametricDistribution.o ContinuousDistribution.o GaussianDistribution.o GammaDistribution.o ContinuousMultimixtureDistr.o DecayContinuousMultimixtureDistr.o SearchResult.o SequestResult.o MassDifferenceDiscrMixtureDistr.o IsoMassDiffDiscrMixtureDistr.o MzDifferenceDiscrMixtureDistr.o VariableOffsetMassDiffDiscrMixtureDistr.o ExtremeValueDistribution.o GlycMixtureDistr.o PeptideProphetParser.o Parser.o OrderedResult.o PeptideProphetOptions.o Spectrum.o TPPVersionInfo.o win32_system.o TagFilter.o MascotDiscrimValMixtureDistr.o MascotDiscrimFunction.o MascotResult.o MixtureDistrFactory.o MascotScoreParser.o CometDiscrimValMixtureDistr.o CometDiscrimFunction.o CometResult.o TandemDiscrimValMixtureDistr.o TandemDiscrimFunction.o TandemNativeDF.o TandemKscoreDF.o  TandemResult.o PhenyxDiscrimValMixtureDistr.o PhenyxDiscrimFunction.o PhenyxZscore.o PhenyxResult.o Option.o pIMixtureDistr.o pICalculator.o RTMixtureDistr.o RTCalculator.o ModificationInfo.o VariableOffsetpIMixtureDistr.o VariableOffsetRTMixtureDistr.o ResidueMass.o ProbIDResult.o ProbIDDiscrimFunction.o ProbIDDiscrimValMixtureDistr.o SpectraSTDiscrimValMixtureDistr.o SpectraSTDiscrimFunction.o SpectraSTResult.o ProteolyticEnzyme.o SearchParams.o util.o ASAPRatio_numFns.o ASAPRatio_pepFns.o ASAPRatio_txtFns.o Normalization.o PvalueModel.o SequestResult.o MascotResult.o CometResult.o TandemResult.o PhenyxResult.o Parser.o Tag.o TagListComparator.o TPPVersionInfo.o win32_system.o TagFilter.o util.o MixtureModel.o MixtureDistr.o ramp.o cramp.o base64.o DiscrimValMixtureDistr.o DiscreteMixtureDistr.o NTTMixtureDistr.o NMCMixtureDistr.o ICATMixtureDistr.o DiscriminantFunction.o SequestDiscrimFunction.o AbbrevSequestDiscrimFunction.o ToftofAbbrevSequestDiscrimFunction.o Distribution.o DiscreteDistribution.o NonParametricDistribution.o ContinuousDistribution.o GaussianDistribution.o GammaDistribution.o ContinuousMultimixtureDistr.o DecayContinuousMultimixtureDistr.o SearchResult.o SequestResult.o MassDifferenceDiscrMixtureDistr.o IsoMassDiffDiscrMixtureDistr.o VariableOffsetMassDiffDiscrMixtureDistr.o ExtremeValueDistribution.o GlycMixtureDistr.o PeptideProphetParser.o OrderedResult.o PeptideProphetOptions.o Spectrum.o MascotDiscrimValMixtureDistr.o MascotDiscrimFunction.o MascotResult.o MixtureDistrFactory.o MascotScoreParser.o CometDiscrimValMixtureDistr.o CometDiscrimFunction.o CometResult.o TandemDiscrimValMixtureDistr.o TandemDiscrimFunction.o TandemNativeDF.o TandemKscoreDF.o   TandemResult.o PhenyxDiscrimValMixtureDistr.o PhenyxDiscrimFunction.o PhenyxZscore.o PhenyxResult.o Option.o pIMixtureDistr.o pICalculator.o ModificationInfo.o VariableOffsetpIMixtureDistr.o ResidueMass.o ProbIDResult.o ProbIDDiscrimFunction.o ProbIDDiscrimValMixtureDistr.o SpectraSTResult.o SpectraSTDiscrimFunction.o SpectraSTDiscrimValMixtureDistr.o ProteolyticEnzyme.o SearchParams.o UniquePeptide.o OuterTag.o SequestParams.o SearchParams.o ProteolyticEnzymeFactory.o ModifiedResidueMass.o tpp_tarball.o SequestOut.o SequestHit.o ProDataComponent.o

libtpp.a: ${TPPLIB_OBJS}
	ar -r libtpp.a ${TPPLIB_OBJS}

PeptideProphetParser: PeptideProphetParserMain.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS)

ProteinProphet: ProteinProphet.o ppPeptide.o libtpp.a ppProtein.o ppParentProtein.o ppSpectrum.o 
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS) $(BOOST_REGEX_LIB)

EnzymeDigestionParser:  EnzymeDigestionParserMain.o EnzymeDigestionParser.o  ProteolyticEnzymeFactory.o ProteolyticEnzyme.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^   

XPressPeptideParser:  XPressPeptideParserMain.o XPressPeptideParser.o libtpp.a  
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

ASAPRatioPeptideParser:  ASAPRatioPeptideParserMain.o ASAPRatioPeptideParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

xpress-prophet-update.cgi : XPressCGIParser.o xpresscgimain.o libtpp.a
	$(LD) $(DEBUG)  -O2 -o $@ $^	

ASAPRatioPeptideCGIDisplayParser.cgi:  ASAPRatioPeptideCGIDisplayParserMain.o ASAPRatioPeptideCGIDisplayParser.o ASAPRatioPeptideUpdateParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

ASAPRatioProteinRatioParser :  ASAPRatioProteinRatioParserMain.o ASAPRatioProteinRatioParser.o ASAPRatioGroupPeptideParser.o ASAPProteinRatio.o ASAPRatioPeptideParser.o ASAPRatioMSMSSeqData.o ASAPRatioPvalueParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

#
ASAPCGIDisplay.cgi :  ASAPCGIDisplay2Main.o ASAPRatioProteinCGIDisplayParser.o ASAPCGIDisplayParser.o ASAPCGIParser.o libtpp.a  
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz


InteractParser:  InteractParserMain.o InteractParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ 

DatabaseParser:  DatabaseParserMain.o DatabaseParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^

ifeq (${LGPL},1)
RefreshParser:  RefreshParserMain.o spareparts.a RefreshParser.o libtpp.a
	$(LD) ${CFLAGS} $(DEBUG)   -O2 -o $@ $^ ${LDFLAGS} 
else
RefreshParser:  RefreshParserMain.o RefreshParser.o libtpp.a kwset.o obstack.o
	$(LD) ${CFLAGS} $(DEBUG)   -O2 -o $@ $^ ${LDFLAGS} 
endif

MzXML2Search:  MzXML2Search.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

ModelParser.cgi : ModelParserMain.o ModelParser.o libtpp.a
	$(LD) $(DEBUG) -O2 -o $@ $^


XPressPeptideUpdateParser.cgi:  XPressPeptideUpdateParserMain.o XPressPeptideUpdateParser.o  libtpp.a xgd.o xgdfonts.o
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

#XPressPeptideUpdateParser:  XPressPeptideUpdateParserMain.o XPressPeptideUpdateParser.o  libtpp.agd.o gdfonts.o 
#        $(LD) $(DEBUG)   -O2 -o $@ $^ -lz

XPressProteinRatioParser :  XPressProteinRatioParserMain.o XPressProteinRatioParser.o XPressGroupPeptideParser.o  libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

XPressCGIProteinDisplayParser.cgi :  XPressCGIProteinDisplayMain.o XPressProteinRatioParser.o XPressCGIProteinDisplay.o XPressGroupPeptideParser.o  libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

Q3ProteinRatioParser :  Q3ProteinRatioParserMain.o Q3ProteinRatioParser.o Q3GroupPeptideParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^

ASAPRatioPvalueParser :  ASAPRatioPvalueParserMain.o ASAPRatioPvalueParser.o libtpp.a 
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lm


LibraPeptideParser :  LibraPeptideParserMain.o LibraPeptideParser.o libtpp.a Intensity.o Quantitation.o LibraConditionHandler.o LibraResult.o LibraSummary.o LibraWrapper.o
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

LibraProteinRatioParser : LibraProteinRatioParserMain.o LibraProteinRatioParser.o LibraGroupPeptideParser.o libtpp.a LibraConditionHandler2.o StringConvertor.o 
	$(LD) $(DEBUG)   -O2 -o $@ $^


#
Pep3D_xml.cgi :  cgi_Pep3D_xml.o Pep3D_functions.o Pep3DParser.o BasenameParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(GD_LIB) -lm  -lpng -lz

#
disCidData_xml.cgi:  cgi_disCidData.o  util.o Pep3D_functions.o SpectrumParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^  -lm -lz

#
getCidData_xml.cgi:  cgi_getCidData.o Pep3D_functions.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lm -lz


AnalysisSummaryParser.cgi :  AnalysisSummaryParserMain.o AnalysisSummaryParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^

CompactParser :  CompactParserMain.o CompactParser.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^

Sequest2XML :  Sequest2XML.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

Out2XML :   Out2XMLMain.o Out2XML.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

Sqt2XML :   Sqt2XML.o SimpleXMLWriter.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz

CombineOut :   CombineOutMain.o CombineOut.o Out2XML.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz


Mascot2XML :  MascotConverterMain.o MascotConverter.o MascotModification.o libtpp.a libarchive.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ libarchive.a $(LDFLAGS) $(WWW_LIBS) -lz

Comet2XML :  Comet2xml.o ProteolyticEnzymeFactory.o libtpp.a
	 $(LD) $(DEBUG)   -O2 -o $@ $^ -lz

# build expat library?
libexpat.a: 
	@if [ ! -e $(EXPAT) ]; then echo Error: expat source not found!; exit -1; fi;
	@if [ ! -e $(EXPAT)/.libs/libexpat.a ]; then echo building expat library; cd $(EXPAT); ./configure; make; fi;
	@cp $(EXPAT)/.libs/libexpat.a .

# build archive library?
libarchive.a:
	@if [ ! -e $(LIBARCHIVE) ]; then echo Error: libarchive source not found!; exit -1; fi;
	@if [ ! -e $(LIBARCHIVE)/.libs/libarchive.a ]; then echo building libarchive library; cd $(LIBARCHIVE); chmod 777 configure; ./configure; make; fi;
	@cp $(LIBARCHIVE)/.libs/libarchive.a .


Tandem2XML :  Tandem2xml.o TandemParamsParser.o TandemResultsParser.o masscalc.o saxhandler.o libtpp.a libexpat.a
	 $(LD) $(DEBUG)   -O2 -o $@ $^ -lz

comet-fastadb.cgi :  comet-fastadb1.o calc_pI.o libtpp.a 
	$(LD) $(DEBUG)   -O2 -o $@ $^

plot-msms.cgi :  plot-msms1.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS) -lz

dta-xml.cgi :  dta-xml.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS) -lz

sequest-tgz-out.cgi :  sequest-tgz-out1.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS)

cometresult.cgi :  cometresult1.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS)

plotspectrast : plotspectrast.o FileUtils.o Peptide.o SpectraSTLibEntry.o SpectraSTQuery.o SpectraSTPeakList.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz $(LDFLAGS)

plotspectrast.cgi : plotspectrast_cgi.o FileUtils.o Peptide.o SpectraSTLibEntry.o SpectraSTQuery.o SpectraSTPeakList.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ -lz $(LDFLAGS)

spectrast : SpectraSTLib.o SpectraSTLibIndex.o SpectraSTLibEntry.o SpectraSTPeakList.o SpectraSTLibImporter.o SpectraSTMspLibImporter.o SpectraSTXHunterLibImporter.o SpectraSTSearchTask.o SpectraSTMspSearchTask.o SpectraSTMzXMLSearchTask.o SpectraSTDtaSearchTask.o SpectraSTCandidate.o SpectraSTQuery.o SpectraSTSearch.o SpectraSTSearchOutput.o SpectraSTTxtSearchOutput.o SpectraSTXlsSearchOutput.o SpectraSTPepXMLSearchOutput.o SpectraSTMzLibIndex.o SpectraSTPeptideLibIndex.o SpectraSTSimScores.o SpectraSTSearchParams.o SpectraSTCreateParams.o SpectraSTMain.o FileUtils.o Peptide.o ProgressCount.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^ $(LDFLAGS) -lz


xinteract :  xinteract.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^


runperl:   runperl.o libtpp.a
	$(LD) $(DEBUG)   -O2 -o $@ $^

PepXMLViewer.cgi:
	cd Visualization/PepXMLViewer; make

out2summary: out2summary.o
	gcc -O2 -o $@ $^

runsearch: runsearch1.o
	$(LD) $(DEBUG)    -O2 -o $@ $^

dtafilter: dtafilter1.o
	$(LD) $(DEBUG)    -O2 -o $@ $^

readmzXML: base64.o ramp.o
	make -C mzXML/tools/readmzXML
	cp mzXML/tools/readmzXML/readmzXML.exe .

dta2mzxml:
	make -C mzXML/converters/dta2mzxml
	cp mzXML/converters/dta2mzxml/dta2mzxml .

clean:
	rm -f *.o *.a *.exe *.cgi $(EXE) core* ../COVERAGE/*.o ../COVERAGE/*.exe refresh_interact/*.o $(FASTAR)/*.o $(FASTAR)/fastar/*.exe
	cd $(EXPAT); if [ -e Makefile ]; then make distclean; fi
	cd Visualization/PepXMLViewer; make clean
	make -C mzXML/tools/readmzXML clean
	make -C mzXML/converters/dta2mzxml clean

LGPL:
	LGPL='1' make all

opt:
	CCOPT='-O' make all

debug:
	DEBUG='-O2' make all


xtandem:
	make -C ../extern/tandem-linux-07-07-01-2/src


windows:  # makefile is smarter now, don't really need this target
	make all;

ifeq (${XML_ONLY},1)
install-windows: install-windows-xml
else
install-windows: install-windows-all
endif

install-windows-xml:
	if ! test -d `cygpath -u ${TPP_ROOT}`; then \
		mkdir `cygpath -u ${TPP_ROOT}`;  fi
	if ! test -d `cygpath -u ${TPP_ROOT}`/bin; then \
		mkdir `cygpath -u ${TPP_ROOT}`/bin;  fi
	if ! test -d `cygpath -u ${TPP_ROOT}`/schema; then \
		mkdir `cygpath -u ${TPP_ROOT}`/schema;  fi
	cp -f ../schema/*.xsd ../schema/*.xsl `cygpath -u ${TPP_ROOT}`/schema
	cp -f *.exe ../perl/*.pl ../COVERAGE/*.exe `cygpath -u ${TPP_ROOT}`/bin

install-windows-all: 
	cp -f ../schema/*.xsd ../schema/*.xsl $$WEBSERVER_ROOT
	if ! test -d `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin; then \
		mkdir `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin;  fi
	cp -f *.exe ../perl/*.pl ../perl/*.par ../COVERAGE/*.exe /cygdrive/c/cygwin/bin
	cp -f *.cgi `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin
	cp -f Visualization/Comet/tryp_model.txt `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin
	cp -f ../CGI/*.pl `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin
	cp -f ../HELP_DIR/*.* `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin
	cp -f ../images/*.* `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin
	sed -i 's?/tpp/cgi-bin/?/tpp-bin/?g' `cygpath -u $$WEBSERVER_ROOT`/pepXML_std.xsl
	chmod -R a+xr `cygpath -u $$WEBSERVER_ROOT`/../tpp-bin
	cp -f Visualization/Comet/cometlinks.def `cygpath -u $$WEBSERVER_ROOT`
	chmod a+r `cygpath -u $$WEBSERVER_ROOT`/cometlinks.def
	make -C Visualization/PepXMLViewer/ install-windows


#
# dependencies (incomplete)
#
ifeq (${LGPL},1)
spareparts.a : 
	cd $(FASTAR); make all
	ar -rs spareparts.a $(FASTAR)/*.o $(FASTAR)/common/*.o
endif


ProteinProphet.o: ppProtein.h

xinteract.o : xinteract.cxx Array.h constants.h

XPressCGIParser.o : Tag.h TagFilter.h Parser.h XPressCGIParser.h XPressCGIParser.cxx

xpresscgimain.o :  XPressCGIParser.h xpresscgimain.cxx

SpectrumParser.o : SpectrumParser.cxx SpectrumParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ASAP_structs.h ProDataComponent.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestResult.h MascotResult.h CometResult.h TandemResult.h PhenyxResult.h ASAPRatio_numFns.h

cgi_Pep3D_xml.o: cgi_Pep3D_xml.c Pep3D_functions.h ramp.h base64.h util.h constants.h ASAPRatio_numFns.h Pep3DParser.h Parser.h Tag.h Array.h sysdepend.h TagFilter.h BasenameParser.h

Pep3D_functions.o : Pep3D_functions.c Pep3D_functions.h ramp.h base64.h constants.h ASAPRatio_numFns.h

Pep3DParser.o : Pep3DParser.cxx Pep3DParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

Pep3DParserMain.o : Pep3DParserMain.cxx Pep3DParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

cgi_getCidData.o : cgi_getCidData.c Pep3D_functions.h ramp.h base64.h constants.h ASAPRatio_numFns.h

cgi_disCidData.o : cgi_disCidData.c Pep3D_functions.h ramp.h base64.h constants.h ASAPRatio_numFns.h SpectrumParser.h Parser.h Tag.h Array.h sysdepend.h TagFilter.h ASAP_structs.h ProDataComponent.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ResidueMass.h constants.h SequestResult.h MascotResult.h CometResult.h TandemResult.h PhenyxResult.h

BasenameParser.o : BasenameParser.cxx BasenameParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

InteractParser.o : InteractParser.cxx InteractParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h OuterTag.h

InteractParserMain.o : InteractParserMain.cxx InteractParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h OuterTag.h

sequest-tgz-out1.o : sequest-tgz-out1.c Comet.h constants.h

cometresult1.o : cometresult1.c Comet.h constants.h

plot-msms1.o : plot-msms1.c AminoAcidMasses.h Comet.h constants.h

dta-xml.o : dta-xml.c constants.h

cramp.o : cramp.cpp cramp.hpp 

tpp_tarball.o : tpp_tarball.h tpp_tarball.cpp libarchive.a

calc_pI.o : calc_pI.c

comet-fastadb1.o : comet-fastadb1.cxx AminoAcidMasses.h Comet.h constants.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Array.h Tag.h sysdepend.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

PeptideProphetMain.o : PeptideProphetMain.cxx GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h GaussianDistribution.h DiscreteDistribution.h MixtureDistr.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h DiscrimValMixtureDistr.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h ICATMixtureDistr.h MixtureModel.h NMCMixtureDistr.h GlycMixtureDistr.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h Parser.h TagFilter.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h TandemDiscrimValMixtureDistr.h TandemDiscrimFunction.h TandemResult.h PhenyxDiscrimValMixtureDistr.h PhenyxDiscrimFunction.h PhenyxResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

PeptideProphetParser.o : PeptideProphetParser.cxx PeptideProphetParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h TandemDiscrimValMixtureDistr.h TandemDiscrimFunction.h TandemResult.h PhenyxDiscrimValMixtureDistr.h PhenyxDiscrimFunction.h PhenyxResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h  RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h ProteolyticEnzyme.h

PeptideProphetParserMain.o : PeptideProphetParserMain.cxx MixtureDistrFactory.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestResult.h MascotResult.h ProbIDResult.h DiscrimValMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotScoreParser.h Parser.h TagFilter.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h TandemDiscrimValMixtureDistr.h TandemDiscrimFunction.h TandemResult.h PhenyxDiscrimValMixtureDistr.h PhenyxDiscrimFunction.h PhenyxResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h NMCMixtureDistr.h MassDifferenceDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h ICATMixtureDistr.h GlycMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h PeptideProphetParser.h MixtureModel.h Spectrum.h OrderedResult.h PeptideProphetOptions.h ProteolyticEnzyme.h

PeptideProphetOptions.o : PeptideProphetOptions.cxx PeptideProphetOptions.h

ModelParser.o : ModelParser.cxx ModelParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h PeptideProphetParser.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h TandemDiscrimValMixtureDistr.h TandemDiscrimFunction.h TandemResult.h PhenyxDiscrimValMixtureDistr.h PhenyxDiscrimFunction.h PhenyxResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h ProteolyticEnzyme.h

ModelParserMain.o : ModelParserMain.cxx ModelParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h PeptideProphetParser.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h TandemDiscrimValMixtureDistr.h TandemDiscrimFunction.h TandemResult.h PhenyxDiscrimValMixtureDistr.h PhenyxDiscrimFunction.h PhenyxResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h ProteolyticEnzyme.h

MixtureModel.o : MixtureModel.cxx MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h Parser.h TagFilter.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h TandemDiscrimValMixtureDistr.h TandemDiscrimFunction.h TandemResult.h PhenyxDiscrimValMixtureDistr.h PhenyxDiscrimFunction.h PhenyxResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

OrderedResult.o : OrderedResult.cxx OrderedResult.h

Spectrum.o : Spectrum.cxx Spectrum.h

ContinuousMultimixtureDistr.o : ContinuousMultimixtureDistr.cxx ContinuousMultimixtureDistr.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h GaussianDistribution.h GammaDistribution.h

DecayContinuousMultimixtureDistr.o : DecayContinuousMultimixtureDistr.cxx DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h GaussianDistribution.h GammaDistribution.h NTTMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

DiscreteMixtureDistr.o : DiscreteMixtureDistr.cxx DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

GlycMixtureDistr.o : GlycMixtureDistr.cxx GlycMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ICATMixtureDistr.o : ICATMixtureDistr.cxx ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

MassDifferenceDiscrMixtureDistr.o : MassDifferenceDiscrMixtureDistr.cxx MassDifferenceDiscrMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

IsoMassDiffDiscrMixtureDistr.o : IsoMassDiffDiscrMixtureDistr.cxx IsoMassDiffDiscrMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

MzDifferenceDiscrMixtureDistr.o : MzDifferenceDiscrMixtureDistr.cxx MzDifferenceDiscrMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h SpectraSTResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

MixtureDistr.o : MixtureDistr.cxx MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

MixtureDistrFactory.o : MixtureDistrFactory.cxx MixtureDistrFactory.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestResult.h MascotResult.h ProbIDResult.h DiscrimValMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotScoreParser.h Parser.h TagFilter.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h SpectraSTDiscrimValMixtureDistr.h SpectraSTDiscrimFunction.h SpectraSTResult.h NMCMixtureDistr.h MassDifferenceDiscrMixtureDistr.h IsoMassDiffDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h ICATMixtureDistr.h GlycMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

NMCMixtureDistr.o : NMCMixtureDistr.cxx NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

NTTMixtureDistr.o : NTTMixtureDistr.cxx NTTMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

pIMixtureDistr.o : pIMixtureDistr.cxx pIMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h pICalculator.h ICATMixtureDistr.h

RTMixtureDistr.o : RTMixtureDistr.cxx RTMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h pICalculator.h ICATMixtureDistr.h

VariableOffsetMassDiffDiscrMixtureDistr.o : VariableOffsetMassDiffDiscrMixtureDistr.cxx VariableOffsetMassDiffDiscrMixtureDistr.h MassDifferenceDiscrMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

VariableOffsetpIMixtureDistr.o : VariableOffsetpIMixtureDistr.cxx VariableOffsetpIMixtureDistr.h pIMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h pICalculator.h ICATMixtureDistr.h

VariableOffsetRTMixtureDistr.o : VariableOffsetRTMixtureDistr.cxx VariableOffsetRTMixtureDistr.h RTMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h RTCalculator.h ICATMixtureDistr.h

NonParametricDistribution.o : NonParametricDistribution.cxx NonParametricDistribution.h ContinuousDistribution.cxx GammaDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h 

ContinuousDistribution.o : ContinuousDistribution.cxx NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h

DiscreteDistribution.o : DiscreteDistribution.cxx DiscreteDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h

Distribution.o : Distribution.cxx Distribution.h sysdepend.h constants.h Tag.h Array.h

ExtremeValueDistribution.o : ExtremeValueDistribution.cxx ExtremeValueDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h

GammaDistribution.o : GammaDistribution.cxx GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h

GaussianDistribution.o : GaussianDistribution.cxx GaussianDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h Array.h

DiscriminantFunction.o : DiscriminantFunction.cxx DiscriminantFunction.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

DiscrimValMixtureDistr.o : DiscrimValMixtureDistr.cxx DiscrimValMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h

ToftofAbbrevSequestDiscrimFunction.o : ToftofAbbrevSequestDiscrimFunction.cxx ToftofAbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h DiscriminantFunction.h

AbbrevSequestDiscrimFunction.o : AbbrevSequestDiscrimFunction.cxx AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h DiscriminantFunction.h

SequestDiscrimFunction.o : SequestDiscrimFunction.cxx SequestDiscrimFunction.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h DiscriminantFunction.h

ProbIDDiscrimFunction.o : ProbIDDiscrimFunction.cxx ProbIDDiscrimFunction.h DiscriminantFunction.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h ProbIDResult.h

ProbIDDiscrimValMixtureDistr.o : ProbIDDiscrimValMixtureDistr.cxx ProbIDDiscrimValMixtureDistr.h DiscrimValMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h ProbIDDiscrimFunction.h ProbIDResult.h ExtremeValueDistribution.h

MascotDiscrimFunction.o : MascotDiscrimFunction.cxx MascotDiscrimFunction.h DiscriminantFunction.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h MascotResult.h MascotScoreParser.h Parser.h TagFilter.h

MascotDiscrimValMixtureDistr.o : MascotDiscrimValMixtureDistr.cxx MascotDiscrimValMixtureDistr.h DiscrimValMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h Parser.h TagFilter.h

MascotScoreParser.o : MascotScoreParser.cxx MascotScoreParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h

CometDiscrimFunction.o : CometDiscrimFunction.cxx CometDiscrimFunction.h DiscriminantFunction.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h CometResult.h

CometDiscrimValMixtureDistr.o : CometDiscrimValMixtureDistr.cxx CometDiscrimValMixtureDistr.h DiscrimValMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h ExtremeValueDistribution.h CometDiscrimFunction.h CometResult.h

SpectraSTDiscrimFunction.o : SpectraSTDiscrimFunction.cxx SpectraSTDiscrimFunction.h DiscriminantFunction.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SpectraSTResult.h

SpectraSTDiscrimValMixtureDistr.o : SpectraSTDiscrimValMixtureDistr.cxx SpectraSTDiscrimValMixtureDistr.h DiscrimValMixtureDistr.h MixtureDistr.h Array.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h sysdepend.h constants.h Tag.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h NTTMixtureDistr.h DiscreteMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h ExtremeValueDistribution.h SpectraSTDiscrimFunction.h SpectraSTResult.h

runperl.o : runperl.c

Option.o : Option.cxx Option.h constants.h ramp.h base64.h

XPressGroupPeptideParser.o : XPressGroupPeptideParser.cxx XPressGroupPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

XPressProteinRatioParser.o : XPressProteinRatioParser.cxx XPressProteinRatioParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h XPressGroupPeptideParser.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h  RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

XPressProteinRatioParserMain.o : XPressProteinRatioParserMain.cxx XPressProteinRatioParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h XPressGroupPeptideParser.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h MzDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h  RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

xgd.o : xgd.c xgd.h mtables.c

xgdfonts.o : xgdfonts.c xgdfonts.h xgd.h

mtables.o : mtables.c

#XPressPeptideUpdateParser.o : XPressPeptideUpdateParser.cxx XPressPeptideUpdateParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h gd.c gd.h

XPressPeptideUpdateParserMain.o : XPressPeptideUpdateParserMain.cxx base64.h ramp.h constants.h XPressPeptideUpdateParser.h Parser.h Tag.h Array.h sysdepend.h TagFilter.h Option.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

XPressPeptideParser.o : XPressPeptideParser.cxx XPressPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ResidueMass.h constants.h ModificationInfo.h SearchParams.h

XPressPeptideParserMain.o : XPressPeptideParserMain.cxx XPressPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ResidueMass.h constants.h ModificationInfo.h SearchParams.h

XPressCGIProteinDisplay.o : XPressCGIProteinDisplay.cxx XPressCGIProteinDisplay.h constants.h XPressGroupPeptideParser.h util.h Parser.h Tag.h Array.h sysdepend.h TagFilter.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

XPressCGIProteinDisplayMain.o : XPressCGIProteinDisplayMain.cxx XPressCGIProteinDisplay.h constants.h XPressGroupPeptideParser.h Parser.h Tag.h Array.h sysdepend.h TagFilter.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

RatioParser.o : RatioParser.cxx RatioParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

LibraGroupPeptideParser.o : LibraGroupPeptideParser.cxx LibraGroupPeptideParser.h  LibraConditionHandler2.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

LibraProteinRatioParser.o : LibraProteinRatioParser.cxx LibraProteinRatioParser.h  LibraConditionHandler2.h StringConvertor.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h LibraGroupPeptideParser.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

LibraProteinRatioParserMain.o : LibraProteinRatioParserMain.cxx  LibraProteinRatioParser.cxx  LibraProteinRatioParser.h  LibraConditionHandler2.cpp LibraProteinRatioParser.o LibraConditionHandler2.h StringConvertor.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h  LibraGroupPeptideParser.cxx  LibraGroupPeptideParser.h Option.h ramp.h base64.h MixtureModel.h NMCMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h NTTMixtureDistr.h GlycMixtureDistr.h DiscrimValMixtureDistr.h DecayContinuousMultimixtureDistr.h ContinuousMultimixtureDistr.h DiscriminantFunction.h AbbrevSequestDiscrimFunction.h SequestDiscrimFunction.h SequestResult.h MascotDiscrimValMixtureDistr.h ExtremeValueDistribution.h MascotDiscrimFunction.h MascotResult.h MascotScoreParser.h ICATMixtureDistr.h Spectrum.h OrderedResult.h MassDifferenceDiscrMixtureDistr.h VariableOffsetMassDiffDiscrMixtureDistr.h PeptideProphetOptions.h MixtureDistrFactory.h ProbIDResult.h CometDiscrimValMixtureDistr.h CometDiscrimFunction.h CometResult.h ProbIDDiscrimValMixtureDistr.h ProbIDDiscrimFunction.h pIMixtureDistr.h pICalculator.h VariableOffsetpIMixtureDistr.h RTMixtureDistr.h RTCalculator.h VariableOffsetRTMixtureDistr.h

Intensity.o : Intensity.cpp Intensity.hpp LibraConditionHandler.hpp typedefs.hpp Tag.h Array.h sysdepend.h constants.h Quantitation.hpp base64.h ramp.h

LibraConditionHandler.o : LibraConditionHandler.cpp LibraConditionHandler.hpp typedefs.hpp Tag.h Array.h sysdepend.h constants.h

LibraConditionHandler2.o : LibraConditionHandler2.cpp LibraConditionHandler2.h

StringConvertor.o : StringConvertor.cpp StringConvertor.h

LibraPeptideParser.o : LibraPeptideParser.cxx LibraPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h LibraConditionHandler.hpp typedefs.hpp LibraWrapper.hpp LibraResult.hpp Intensity.hpp Quantitation.hpp LibraSummary.hpp

LibraPeptideParserMain.o : LibraPeptideParserMain.cxx LibraPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h LibraConditionHandler.hpp typedefs.hpp LibraWrapper.hpp LibraResult.hpp Intensity.hpp Quantitation.hpp LibraSummary.hpp

LibraResult.o : LibraResult.cpp LibraResult.hpp Intensity.hpp LibraConditionHandler.hpp typedefs.hpp Tag.h Array.h sysdepend.h constants.h Quantitation.hpp base64.h ramp.h

LibraSummary.o : LibraSummary.cpp LibraSummary.hpp LibraConditionHandler.hpp typedefs.hpp Tag.h Array.h sysdepend.h constants.h

LibraWrapper.o : LibraWrapper.cpp LibraWrapper.hpp LibraConditionHandler.hpp typedefs.hpp Tag.h Array.h sysdepend.h constants.h LibraResult.hpp Intensity.hpp Quantitation.hpp base64.h ramp.h LibraSummary.hpp

Quantitation.o : Quantitation.cpp Quantitation.hpp LibraConditionHandler.hpp typedefs.hpp Tag.h Array.h sysdepend.h constants.h base64.h ramp.h


UniquePeptide.o : UniquePeptide.cxx UniquePeptide.h

PvalueModel.o : PvalueModel.cxx PvalueModel.h constants.h ASAP_structs.h

Normalization.o : Normalization.cxx Normalization.h ASAP_structs.h constants.h

ASAPRatio_numFns.o : ASAPRatio_numFns.c ASAPRatio_numFns.h constants.h

ASAPRatio_pepFns.o : ASAPRatio_pepFns.c ASAPRatio_numFns.h constants.h ASAPRatio_pepFns.h ASAPRatio_txtFns.h base64.h ramp.h

ASAPRatio_txtFns.o : ASAPRatio_txtFns.c ASAPRatio_txtFns.h

ASAPRatioPvalueParser.o : ASAPRatioPvalueParser.cxx ASAPRatioPvalueParser.h Parser.h util.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAP_structs.h PvalueModel.h Normalization.h

ASAPRatioPvalueParserMain.o : ASAPRatioPvalueParserMain.cxx ASAPRatioPvalueParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAP_structs.h PvalueModel.h Normalization.h

ASAPProteinRatio.o : ASAPProteinRatio.cxx ASAPProteinRatio.h ASAPRatioMSMSSeqData.h Array.h constants.h ModificationInfo.h Tag.h sysdepend.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h ASAPRatioPeptideParser.h Parser.h TagFilter.h ASAP_structs.h

ASAPRatioGroupPeptideParser.o : ASAPRatioGroupPeptideParser.cxx ASAPRatioGroupPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ASAPProteinRatio.h ASAPRatioMSMSSeqData.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h ASAPRatioPeptideParser.h ASAP_structs.h UniquePeptide.h

ASAPRatioMSMSSeqData.o : ASAPRatioMSMSSeqData.cxx ASAPRatioMSMSSeqData.h Array.h constants.h ModificationInfo.h Tag.h sysdepend.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ASAPRatioProteinRatioParser.o : ASAPRatioProteinRatioParser.cxx ASAPRatioProteinRatioParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAPRatioGroupPeptideParser.h Option.h ramp.h base64.h ASAPProteinRatio.h ASAPRatioMSMSSeqData.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h ASAPRatioPeptideParser.h ASAP_structs.h UniquePeptide.h ASAPRatioPvalueParser.h PvalueModel.h Normalization.h

ASAPRatioProteinRatioParserMain.o : ASAPRatioProteinRatioParserMain.cxx ASAPRatioProteinRatioParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAPRatioGroupPeptideParser.h Option.h ramp.h base64.h ASAPProteinRatio.h ASAPRatioMSMSSeqData.h ModificationInfo.h SearchParams.h ResidueMass.h constants.h ASAPRatioPeptideParser.h ASAP_structs.h UniquePeptide.h ASAPRatioPvalueParser.h PvalueModel.h Normalization.h

ASAPRatioPeptideParser.o : ASAPRatioPeptideParser.cxx ASAPRatioPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ResidueMass.h constants.h ModificationInfo.h SearchParams.h

ASAPRatioPeptideParserMain.o : ASAPRatioPeptideParserMain.cxx ASAPRatioPeptideParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h ResidueMass.h constants.h ModificationInfo.h SearchParams.h

ASAPRatioPeptideCGIDisplayParser.o : ASAPRatioPeptideCGIDisplayParser.cxx ASAPRatioPeptideCGIDisplayParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h

ASAPRatioPeptideCGIDisplayParserMain.o : ASAPRatioPeptideCGIDisplayParserMain.cxx ASAPRatio_txtFns.h ASAPRatio_numFns.h constants.h ASAPRatio_pepFns.h ASAPRatioPeptideCGIDisplayParser.h Parser.h Tag.h Array.h sysdepend.h TagFilter.h Option.h ramp.h base64.h ASAPRatioPeptideUpdateParser.h

ASAPRatioPeptideUpdateParser.o : ASAPRatioPeptideUpdateParser.cxx ASAPRatioPeptideUpdateParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Option.h ramp.h base64.h

ASAPCGIDisplay2Main.o : ASAPCGIDisplay2Main.cxx ASAPCGIParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAP_structs.h Normalization.h ASAPRatioProteinCGIDisplayParser.h ProDataComponent.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestResult.h MascotResult.h CometResult.h ASAPRatio_numFns.h ASAPCGIDisplayParser.h ASAPRatio_txtFns.h

ASAPCGIDisplayParser.o : ASAPCGIDisplayParser.cxx ASAPCGIDisplayParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAP_structs.h

ASAPCGIParser.o : ASAPCGIParser.cxx ASAPCGIParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h Ratio.h ASAP_structs.h Normalization.h

ASAPRatioProteinCGIDisplayParser.o : ASAPRatioProteinCGIDisplayParser.cxx ASAPRatioProteinCGIDisplayParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ASAP_structs.h ProDataComponent.h SearchResult.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestResult.h MascotResult.h CometResult.h ASAPRatio_numFns.h

ifeq (${LGPL},1)
RefreshParser.o : RefreshParser.cxx RefreshParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ProteolyticEnzyme.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h 

RefreshParserMain.o : RefreshParserMain.cxx RefreshParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ProteolyticEnzyme.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h 
else
RefreshParser.o : RefreshParser.cxx RefreshParser.h kwset.h obstack.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ProteolyticEnzyme.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h 

RefreshParserMain.o : RefreshParserMain.cxx RefreshParser.h kwset.h obstack.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ProteolyticEnzyme.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h 
endif

MzXML2Search.o : MzXML2Search.cxx ramp.h base64.h

base64.o : base64.c base64.h

ramp.o : ramp.c ramp.h base64.h constants.h

OuterTag.o : OuterTag.cxx OuterTag.h Tag.h Array.h sysdepend.h constants.h

Parser.o : Parser.cxx Parser.h Tag.h Array.h sysdepend.h constants.h

Tag.o : Tag.cxx Tag.h Array.h sysdepend.h constants.h

TagFilter.o : TagFilter.cxx TagFilter.h Tag.h Array.h sysdepend.h constants.h

TagListComparator.o : TagListComparator.cxx TagListComparator.h Tag.h Array.h sysdepend.h constants.h

DatabaseParser.o : DatabaseParser.cxx DatabaseParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

DatabaseParserMain.o : DatabaseParserMain.cxx DatabaseParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

CompactParser.o : CompactParser.cxx CompactParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

CompactParserMain.o : CompactParserMain.cxx CompactParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

AnalysisSummaryParser.o : AnalysisSummaryParser.cxx AnalysisSummaryParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

AnalysisSummaryParserMain.o : AnalysisSummaryParserMain.cxx AnalysisSummaryParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h

pICalculator.o : pICalculator.cxx pICalculator.h ModificationInfo.h Array.h Tag.h sysdepend.h constants.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h

RTCalculator.o : RTCalculator.cxx RTCalculator.h ModificationInfo.h Array.h Tag.h sysdepend.h constants.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h

Sequest2XML.o : Sequest2XML.cxx SequestResult.h SequestOut.h SequestHit.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

SequestOut.o : SequestOut.cxx SequestOut.h SequestHit.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

SequestHit.o : SequestHit.cxx SequestHit.h SequestOut.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

Out2XML.o : Out2XMLMain.cxx Out2XML.cxx SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h
Sqt2XML.o : Sqt2XML.cxx SimpleXMLWriter.cpp SimpleXMLWriter.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

CombineOut.o : CombineOutMain.cxx CombineOut.cxx CombineOut.h Out2XML.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

CombineOut.o : CombineOutMain.cxx CombineOut.cxx CombineOut.h Out2XML.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

CombineOutMain.o : CombineOutMain.cxx CombineOut.cxx Out2XML.h SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestParams.h ModifiedResidueMass.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h

SequestParams.o : SequestParams.cxx SequestParams.h SearchParams.h constants.h sysdepend.h Array.h Tag.h ModificationInfo.h Option.h ramp.h base64.h ResidueMass.h constants.h

CometResult.o : CometResult.cxx CometResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

MascotResult.o : MascotResult.cxx MascotResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ProbIDResult.o : ProbIDResult.cxx ProbIDResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

SpectraSTResult.o : SpectraSTResult.cxx SpectraSTResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ProDataComponent.o : ProDataComponent.cxx ProDataComponent.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h SequestResult.h MascotResult.h CometResult.h

SearchResult.o : SearchResult.cxx SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

SequestResult.o : SequestResult.cxx SequestResult.h SearchResult.h sysdepend.h constants.h Tag.h Array.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

SearchParams.o : SearchParams.cxx SearchParams.h constants.h sysdepend.h Array.h Tag.h ModificationInfo.h Option.h ramp.h base64.h ResidueMass.h constants.h

MascotConverter.o : MascotConverter.cxx MascotConverter.h Array.h util.h sysdepend.h constants.h SpectrumPeak.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Tag.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h MascotModification.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h Parser.h

MascotConverterMain.o : MascotConverterMain.cxx MascotConverter.h Array.h sysdepend.h constants.h SpectrumPeak.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Tag.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h MascotModification.h pICalculator.h RTCalculator.h ICATMixtureDistr.h DiscreteMixtureDistr.h MixtureDistr.h GammaDistribution.h NonParametricDistribution.h ContinuousDistribution.h Distribution.h GaussianDistribution.h DiscreteDistribution.h SearchResult.h Parser.h

MascotModification.o : MascotModification.cxx MascotModification.h sysdepend.h constants.h

Comet2xml.o : Comet2xml.cxx Comet.h constants.h AminoAcidMasses.h ramp.h base64.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Array.h Tag.h sysdepend.h ModificationInfo.h SearchParams.h Option.h ResidueMass.h constants.h Parser.h

Tandem2xml.o :  Tandem2xml.cxx TandemParamsParser.h TandemResultsParser.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h Tag.h TagFilter.h ModificationInfo.h SearchParams.h masscalc.h saxhandler.h ramp.h base64.h

TandemParamsParser.o : TandemParamsParser.cxx TandemResultsParser.h ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Parser.h Tag.h TagFilter.h ModificationInfo.h SearchParams.h masscalc.h saxhandler.h ramp.h base64.h

TandemResultsParser.o : TandemResultsParser.cxx ProteolyticEnzymeFactory.h util.h ProteolyticEnzyme.h Parser.h Tag.h TagFilter.h ModificationInfo.h SearchParams.h masscalc.h saxhandler.h ramp.h base64.h

masscalc.o :  masscalc.cxx  masscalc.h

saxhandler.o : saxhandler.cxx saxhandler.h

mascot_summary.o : mascot_summary.cxx

runcompare.o : runcompare.c constants.h

ProteolyticEnzyme.o : ProteolyticEnzyme.cxx ProteolyticEnzyme.h Array.h Tag.h sysdepend.h constants.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ProteolyticEnzymeMain.o : ProteolyticEnzymeMain.cxx ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Array.h Tag.h sysdepend.h constants.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ProteolyticEnzymeFactory.o : ProteolyticEnzymeFactory.cxx ProteolyticEnzymeFactory.h ProteolyticEnzyme.h Array.h Tag.h sysdepend.h constants.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

EnzymeDigestionParser.o : EnzymeDigestionParser.cxx EnzymeDigestionParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ProteolyticEnzyme.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h ProteolyticEnzymeFactory.h

EnzymeDigestionParserMain.o : EnzymeDigestionParserMain.cxx EnzymeDigestionParser.h Parser.h Tag.h Array.h sysdepend.h constants.h TagFilter.h ProteolyticEnzyme.h ModificationInfo.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h ProteolyticEnzymeFactory.h

AspNEnzymeDigestion.o : AspNEnzymeDigestion.cxx AspNEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

ChymotrypticEnzymeDigestion.o : ChymotrypticEnzymeDigestion.cxx ChymotrypticEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

CNBrEnzymeDigestion.o : CNBrEnzymeDigestion.cxx CNBrEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

Elastase.o : Elastase.cxx Elastase.h EnzymeDigestion.h sysdepend.h constants.h

EnzymeDigestion.o : EnzymeDigestion.cxx EnzymeDigestion.h sysdepend.h constants.h

EnzymeSpecificity.o : EnzymeSpecificity.cxx EnzymeSpecificity.h EnzymeDigestion.h sysdepend.h constants.h TrypticEnzymeDigestion.h GluCEnzymeDigestion.h GluC_bicarbEnzymeDigestion.h ChymotrypticEnzymeDigestion.h Elastase.h NonspecificEnzymeDigestion.h TrypChymAspnEnzymeDigestion.h MultipleEnzymeDigestion.h Array.h AspNEnzymeDigestion.h CNBrEnzymeDigestion.h TrypticCNBrEnzymeDigestion.h

GluCEnzymeDigestion.o : GluCEnzymeDigestion.cxx GluCEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

GluC_bicarbEnzymeDigestion.o : GluC_bicarbEnzymeDigestion.cxx GluC_bicarbEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

MultipleEnzymeDigestion.o : MultipleEnzymeDigestion.cxx MultipleEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h Array.h

NonspecificEnzymeDigestion.o : NonspecificEnzymeDigestion.cxx NonspecificEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

TrypChymAspnEnzymeDigestion.o : TrypChymAspnEnzymeDigestion.cxx TrypChymAspnEnzymeDigestion.h MultipleEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h Array.h TrypticEnzymeDigestion.h ChymotrypticEnzymeDigestion.h AspNEnzymeDigestion.h

TrypticCNBrEnzymeDigestion.o : TrypticCNBrEnzymeDigestion.cxx TrypticCNBrEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

TrypticEnzymeDigestion.o : TrypticEnzymeDigestion.cxx TrypticEnzymeDigestion.h EnzymeDigestion.h sysdepend.h constants.h

get_random_number.o : get_random_number.cxx

util.o : util.c

win32_system.o : win32_system.c sysdepend.h

ResidueMass.o : ResidueMass.cxx ResidueMass.h constants.h

ModifiedResidueMass.o : ModifiedResidueMass.cxx ModifiedResidueMass.h constants.h ModificationInfo.h Array.h Tag.h sysdepend.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

ModificationInfo.o : ModificationInfo.cxx ModificationInfo.h Array.h Tag.h sysdepend.h constants.h SearchParams.h Option.h ramp.h base64.h ResidueMass.h constants.h

FileUtils.o : FileUtils.cpp FileUtils.hpp

Peptide.o : Peptide.cpp Peptide.hpp

ProgressCount.o : ProgressCount.cpp ProgressCount.hpp

SpectraSTLib.o : SpectraSTLib.cpp  SpectraSTLib.hpp  SpectraSTLibIndex.hpp  SpectraSTPeptideLibIndex.hpp  SpectraSTLibImporter.hpp  FileUtils.hpp Peptide.hpp

SpectraSTLibIndex.o : SpectraSTLibIndex.cpp  SpectraSTLibIndex.hpp  SpectraSTLibEntry.hpp  FileUtils.hpp SpectraSTConstants.hpp

SpectraSTLibEntry.o : SpectraSTLibEntry.cpp  SpectraSTLibEntry.hpp  SpectraSTPeakList.hpp  FileUtils.hpp Peptide.hpp  

SpectraSTPeakList.o : SpectraSTPeakList.cpp  SpectraSTPeakList.hpp  SpectraSTSimScores.hpp  SpectraSTSearchParams.hpp  FileUtils.hpp

SpectraSTLibImporter.o : SpectraSTLibImporter.cpp SpectraSTLibImporter.hpp  SpectraSTLib.hpp  SpectraSTMspLibImporter.hpp

SpectraSTMspLibImporter.o : SpectraSTMspLibImporter.cpp SpectraSTMspLibImporter.hpp  SpectraSTLibImporter.hpp FileUtils.hpp Peptide.hpp

SpectraSTXHunterLibImporter.o : SpectraSTXHunterLibImporter.cpp SpectraSTXHunterLibImporter.hpp  SpectraSTLibImporter.hpp FileUtils.hpp Peptide.hpp

SpectraSTSearchTask.o : SpectraSTSearchTask.cpp  SpectraSTSearchTask.hpp  SpectraSTLib.hpp SpectraSTSearchOutput.hpp  SpectraSTMspSearchTask.hpp  SpectraSTMzXMLSearchTask.hpp FileUtils.hpp

SpectraSTMspSearchTask.o : SpectraSTMspSearchTask.cpp SpectraSTMspSearchTask.hpp SpectraSTSearchTask.hpp SpectraSTSearch.hpp SpectraSTQuery.hpp SpectraSTPeakList.hpp FileUtils.hpp SpectraSTConstants.hpp

SpectraSTMzXMLSearchTask.o : SpectraSTMzXMLSearchTask.cpp SpectraSTMzXMLSearchTask.hpp SpectraSTSearchTask.hpp SpectraSTSearch.hpp SpectraSTQuery.hpp SpectraSTPeakList.hpp FileUtils.hpp cramp.hpp ramp.h SpectraSTConstants.hpp

SpectraSTDtaSearchTask.o : SpectraSTDtaSearchTask.cpp SpectraSTDtaSearchTask.hpp SpectraSTSearchTask.hpp SpectraSTSearch.hpp SpectraSTQuery.hpp SpectraSTPeakList.hpp FileUtils.hpp SpectraSTConstants.hpp

SpectraSTCandidate.o : SpectraSTCandidate.cpp  SpectraSTCandidate.hpp  SpectraSTLibEntry.hpp  FileUtils.hpp

SpectraSTQuery.o : SpectraSTQuery.cpp SpectraSTQuery.hpp SpectraSTPeakList.hpp FileUtils.hpp

SpectraSTSearch.o : SpectraSTSearch.cpp  SpectraSTSearch.hpp  SpectraSTLib.hpp SpectraSTCandidate.hpp  SpectraSTSearchOutput.hpp  SpectraSTQuery.hpp SpectraSTLibEntry.hpp  FileUtils.hpp

SpectraSTSearchOutput.o : SpectraSTSearchOutput.cpp SpectraSTSearchOutput.hpp  SpectraSTSearchParams.hpp  SpectraSTSimScores.hpp  SpectraSTTxtSearchOutput.hpp  SpectraSTXlsSearchOutput.hpp  SpectraSTPepXMLSearchOutput.hpp  FileUtils.hpp

SpectraSTTxtSearchOutput.o : SpectraSTTxtSearchOutput.cpp SpectraSTTxtSearchOutput.hpp  SpectraSTSearchOutput.hpp Peptide.hpp

SpectraSTXlsSearchOutput.o : SpectraSTXlsSearchOutput.cpp SpectraSTXlsSearchOutput.hpp  SpectraSTSearchOutput.hpp Peptide.hpp

SpectraSTPepXMLSearchOutput.o : SpectraSTPepXMLSearchOutput.cpp SpectraSTPepXMLSearchOutput.hpp  SpectraSTSearchOutput.hpp Peptide.hpp  SpectraSTConstants.hpp

SpectraSTPeptideLibIndex.o : SpectraSTPeptideLibIndex.cpp SpectraSTPeptideLibIndex.hpp  SpectraSTLibEntry.hpp  FileUtils.hpp  Peptide.hpp

SpectraSTMzLibIndex.o : SpectraSTMzLibIndex.cpp SpectraSTMzLibIndex.hpp SpectraSTLibIndex.hpp FileUtils.hpp SpectraSTConstants.hpp

SpectraSTSimScores.o : SpectraSTSimScores.cpp SpectraSTSimScores.hpp

SpectraSTSearchParams.o : SpectraSTSearchParams.cpp SpectraSTSearchParams.hpp FileUtils.hpp SpectraSTConstants.hpp

SpectraSTCreateParams.o : SpectraSTCreateParams.cpp SpectraSTCreateParams.hpp FileUtils.hpp SpectraSTConstants.hpp

SpectraSTMain.o : SpectraSTMain.cpp SpectraSTLib.hpp  SpectraSTLibEntry.hpp  SpectraSTLibIndex.hpp  SpectraSTSearchTask.hpp  SpectraSTSearchParams.hpp  SpectraSTCreateParams.hpp SpectraSTConstants.hpp

plotspectrast.o : plotspectrast.cpp SpectraSTLibEntry.hpp SpectraSTQuery.hpp SpectraSTPeakList.hpp cramp.hpp SpectraSTConstants.hpp

plotspectrast_cgi.o : plotspectrast_cgi.cpp SpectraSTLibEntry.hpp SpectraSTQuery.hpp SpectraSTPeakList.hpp cramp.hpp SpectraSTConstants.hpp

runsearch1.o : runsearch1.c

dtafilter1.o : dtafilter1.c

out2summary.o : out2summary.c
	gcc -O2 -c $^

ifeq (${XML_ONLY},1)
configure-linux: configure-linux-xml
else
configure-linux: configure-linux-html
endif

configure-linux-html:
	#replace the fullpaths
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' common/constants.h
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../perl/ProteinProphet.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/pepxml2html.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/protxml2html.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/findsubsets.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/more_anal.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/more_pepanal.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/prot_wt_xml.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_dataset_derivation.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_help.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_nspbin.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_pepdataset_derivation.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_pipeline_help.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_search_params.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' ../CGI/show_sens_err.pl
	sed -i 's?/tools/bin/TPP/tpp/?${TPP_ROOT}?g' Makefile

	sed -i 's?/tpp/?${TPP_WEB}?g' common/constants.h
	sed -i 's?/tpp/?${TPP_WEB}?g' ../perl/ProteinProphet.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/pepxml2html.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/protxml2html.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/findsubsets.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/more_anal.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/more_pepanal.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/prot_wt_xml.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_dataset_derivation.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_help.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_nspbin.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_pepdataset_derivation.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_pipeline_help.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_search_params.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' ../CGI/show_sens_err.pl
	sed -i 's?/tpp/?${TPP_WEB}?g' Makefile

	sed -i 's?/tpp/cgi-bin/?${CGI_WEB}?g' ../schema/pepXML_std.xsl		
	sed -i 's?/usr/bin/xsltproc?${XSLT_PROC}?g' ../schema/pepXML_std.xsl	
	sed -i 's?/tpp/cgi-bin/?${CGI_WEB}?g' Makefile
	sed -i 's?/usr/bin/xsltproc?${XSLT_PROC}?g' Makefile	
	sed -i 's?^#\([ 	]*\$$XSL_MAKER\)?\1?' ../perl/ProteinProphet.pl

configure-linux-xml: configure-linux-html
	sed -i 's?^\([ 	]*\$$XSL_MAKER\)?#\1?' ../perl/ProteinProphet.pl

ifeq (${XML_ONLY},1)
install-linux: install-linux-xml
else
install-linux: install-linux-all
endif

install-linux-all: install-linux-xml
	# Create Directories
	mkdir -p ${CGI_DIR}
	mkdir -p ${ETC_DIR}
	mkdir -p ${HELP_DIR}
	# HELP_DIR is the same as HTML_DIR for default installations
	if [ ! -e $(HTML_DIR) ]; then mkdir -p ${HTML_DIR}; fi;
	# HELP_DIR is the same as HTML_DIR for default installations
	if [ ! -e $(HTML_DIR)/images ]; then mkdir -p ${HTML_DIR}/images; fi;

	# Call make recursively
	make -C ../CGI/ install
	make -C ../COVERAGE/ install
	make -C ../HELP_DIR/ install
	make -C ../HTML/ install
	make -C ../images/ install
	#PepXMLViewer
	make -C Visualization/PepXMLViewer/ install-linux


	# Copy all source executables and configuration files to their location
	cp -f AnalysisSummaryParser.cgi ${CGI_DIR}
	cp -f ASAPCGIDisplay.cgi ${CGI_DIR}
	cp -f ASAPRatioPeptideCGIDisplayParser.cgi ${CGI_DIR}
	cp -f comet-fastadb.cgi ${CGI_DIR}
	cp -f Visualization/Comet/cometlinks.def ${ETC_DIR}
	cp -f Visualization/Comet/tryp_model.txt ${HELP_DIR}
	cp -f disCidData_xml.cgi ${CGI_DIR}
	cp -f getCidData_xml.cgi ${CGI_DIR}
	cp -f ModelParser.cgi ${CGI_DIR}
	cp -f Pep3D_xml.cgi ${CGI_DIR}
	cp -f plot-msms.cgi ${CGI_DIR}
	cp -f dta-xml.cgi ${CGI_DIR}
	cp -f sequest-tgz-out.cgi ${CGI_DIR}
	cp -f cometresult.cgi ${CGI_DIR}
	cp -f xpress-prophet-update.cgi ${CGI_DIR}
	cp -f XPressCGIProteinDisplayParser.cgi ${CGI_DIR}
	cp -f XPressPeptideUpdateParser.cgi ${CGI_DIR}
	cp -f plotspectrast.cgi ${CGI_DIR}
	chmod a+x ${CGI_DIR}*
	chmod a+r ${HELP_DIR}*

install-linux-xml:
	# Create Directories
	mkdir -p ${TPP_ROOT}
	mkdir -p ${BIN_DIR}
	mkdir -p ${SCHEMA_DIR}
	# Copy all source executables and configuration files to their location
	cp -f ASAPRatioPeptideParser ${BIN_DIR}
	cp -f ASAPRatioProteinRatioParser ${BIN_DIR}
	cp -f ASAPRatioPvalueParser ${BIN_DIR}
	cp -f Comet2XML ${BIN_DIR}
	cp -f CompactParser ${BIN_DIR}
	cp -f DatabaseParser ${BIN_DIR}
	cp -f EnzymeDigestionParser ${BIN_DIR}
	cp -f InteractParser ${BIN_DIR}
	cp -f LibraPeptideParser ${BIN_DIR}
	cp -f LibraProteinRatioParser ${BIN_DIR}
	cp -f Mascot2XML ${BIN_DIR}
	cp -f PeptideProphetParser ${BIN_DIR}
	cp -f ProteinProphet ${BIN_DIR}
	cp -f ../perl/ProteinProphet.pl ${BIN_DIR}
	cp -f ../perl/TPPVersionInfo.pl ${BIN_DIR}
	cp -f ../perl/SSRCalc3.pl ${BIN_DIR}
	cp -f ../perl/SSRCalc3.par ${BIN_DIR}
	cp -f RefreshParser ${BIN_DIR}
	cp -f MzXML2Search ${BIN_DIR}
	cp -f runperl ${BIN_DIR}
	cp -f Sequest2XML ${BIN_DIR}
	cp -f Out2XML ${BIN_DIR}
	cp -f Sqt2XML ${BIN_DIR}
	cp -f CombineOut ${BIN_DIR}
	cp -f Tandem2XML ${BIN_DIR}
	cp -f xinteract ${BIN_DIR}
	cp -f XPressPeptideParser ${BIN_DIR}
	cp -f XPressProteinRatioParser ${BIN_DIR}
	cp -f Q3ProteinRatioParser ${BIN_DIR}
	cp -f spectrast ${BIN_DIR}
	cp -f plotspectrast ${BIN_DIR}
	cp -f runsearch ${BIN_DIR}
	cp -f dtafilter ${BIN_DIR}
	cp -f out2summary ${BIN_DIR} # to be retired in favor of out2xml
	cp -f ../schema/msms_analysis3.dtd ${SCHEMA_DIR}
	cp -f ../schema/pepXML_std.xsl ${SCHEMA_DIR}
	cp -f ../schema/pepXML_v18.xsd ${SCHEMA_DIR}
	cp -f ../schema/pepXML_v9.xsd ${SCHEMA_DIR}
	cp -f ../schema/protXML_v1.xsd ${SCHEMA_DIR}
	cp -f ../schema/protXML_v3.xsd ${SCHEMA_DIR}
	cp -f ../schema/protXML_v4.xsd ${SCHEMA_DIR}
	chmod g+x ${BIN_DIR}*
	chmod a+r ${SCHEMA_DIR}*
# DO NOT DELETE
