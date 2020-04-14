ifeq (${OSTYPE},linux)
all : build build/boost build/xtandem build/tpp
else
all : build build/boost build/expat build/zlib build/xtandem build/tpp build/graphviz
endif

clean :
	rm -rf build
	rm -rf deploy

build :
	mkdir build
	mkdir deploy
	mkdir deploy/bin

build/boost : src/boost_1_33_1.tar.gz
	tar -xzf src/boost_1_33_1.tar.gz -C build
	cd build/boost_1_33_1 && ./configure --prefix=../../deploy
	cd build/boost_1_33_1 && make install
	ln -s boost-1_33_1/boost deploy/include/boost
	date > build/boost

build/expat : src/expat-2.0.0.tar.gz
	tar -xzf src/expat-2.0.0.tar.gz -C build
	cd build/expat-2.0.0 && ./configure --prefix=`pwd`/../../deploy
	cd build/expat-2.0.0 && make install
	date > build/expat


build/zlib : src/zlib-1.2.3.tar.gz
	tar -xzf src/zlib-1.2.3.tar.gz -C build
	cd build/zlib-1.2.3 && ./configure --prefix=../../deploy
	cd build/zlib-1.2.3 && make install
	date > build/zlib

build/graphviz : src/graphviz-2.12.tar.gz
	tar -xzf src/graphviz-2.12.tar.gz -C build
	cd build/graphviz-2.12 && ./configure --prefix=`pwd`/../../deploy --with-mylibgd
	cd build/graphviz-2.12 && make install
	date > build/graphviz

build/xtandem :
	rm -rf build/tandem
	svn co https://svn.mgt.labkey.host/stedi/tags/tandem_2007-07-01 build/tandem
	cp -rf src/replace/tandem/* build/tandem
	cd build/tandem/src && make all
	cp build/tandem/bin/tandem.exe deploy/bin
	date > build/xtandem

build/tpp :
	rm -rf build/trans_proteomic_pipeline
	svn co http://sashimi.svn.sourceforge.net/svnroot/sashimi/tags/release_3-4-1/trans_proteomic_pipeline/ build/trans_proteomic_pipeline
	cp -rf src/replace/trans_proteomic_pipeline/* build/trans_proteomic_pipeline
	cd build/trans_proteomic_pipeline/src && make all install
	cp build/trans_proteomic_pipeline/COVERAGE/batchcoverage deploy/bin
	date > build/tpp
		
