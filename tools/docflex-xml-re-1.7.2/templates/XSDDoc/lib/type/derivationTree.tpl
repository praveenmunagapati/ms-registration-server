<DOCFLEX_TEMPLATE VER='1.12'>
CREATED='2005-04-26 03:31:00'
LAST_UPDATE='2009-02-07 01:37:16'
DESIGNER_TOOL='DocFlex SDK 1.x'
DESIGNER_LICENSE_TYPE='Filigris Works Team'
APP_ID='docflex-xml-xsddoc2'
APP_NAME='DocFlex/XML XSDDoc'
APP_VER='2.1.0'
APP_AUTHOR='Copyright \u00a9 2005-2009 Filigris Works,\nLeonid Rudy Softwareprodukte. All rights reserved.'
TEMPLATE_TYPE='DocumentTemplate'
DSM_TYPE_ID='xsddoc'
ROOT_ETS={'xs:%complexType';'xs:%simpleType'}
<TEMPLATE_PARAMS>
	PARAM={
		param.name='context_comp_id';
		param.title='Context Component ID';
		param.description='The element ID of the component whose detailed documentation is curently being generated. When this is the type itself, whose derivation tree will be printed by this tempate, the hyperlink from the lowerest tree node will be disabled.';
		param.type='Object';
	}
	PARAM={
		param.name='fmt.page.refs';
		param.title='Generate page references';
		param.type='boolean';
		param.default.value='true';
	}
</TEMPLATE_PARAMS>
FMT={
	doc.lengthUnits='pt';
	doc.hlink.style.link='cs4';
}
<STYLES>
	CHAR_STYLE={
		style.name='Default Paragraph Font';
		style.id='cs1';
		style.default='true';
	}
	PAR_STYLE={
		style.name='Derivation Tree Heading';
		style.id='s1';
		text.font.style.bold='true';
		text.color.foreground='#990000';
		par.margin.bottom='6.8';
		par.page.keepWithNext='true';
	}
	CHAR_STYLE={
		style.name='Derivation Tree Method';
		style.id='cs2';
		text.font.name='Verdana';
		text.font.size.relative='70';
		text.color.foreground='#F59200';
	}
	CHAR_STYLE={
		style.name='Derivation Tree Type';
		style.id='cs3';
		text.font.name='Courier New';
		text.font.size.relative='90';
	}
	CHAR_STYLE={
		style.name='Hyperlink';
		style.id='cs4';
		text.decor.underline='true';
		text.color.foreground='#0000FF';
	}
	PAR_STYLE={
		style.name='Normal';
		style.id='s2';
		style.default='true';
	}
	CHAR_STYLE={
		style.name='Page Number Small';
		style.id='cs5';
		text.font.name='Courier New';
		text.font.size='8';
	}
</STYLES>
<ROOT>
	<ELEMENT_ITER>
		TARGET_ETS={'#CUSTOM';'xs:%complexType';'xs:%simpleType';'xs:union'}
		SCOPE='advanced-location-rules'
		RULES={
			'(xs:%complexType|xs:%simpleType) -> .';
			'xs:%complexType -> {(base = getValueByLPath ("(xs:simpleContent|xs:complexContent) / (xs:restriction|xs:extension)/@base")) != null ?\n{ \n  typeQName = toQName (base);\n\n  ((el = findElementByKey ("types", typeQName)) == null) ?\n    el = CustomElement (typeQName.toXMLName (rootElement));\n\n  Enum (el)\n}}::(#CUSTOM|xs:complexType|xs:simpleType)',recursive;
			'xs:%simpleType -> (xs:list|xs:restriction|xs:union)',recursive;
			'(xs:restriction|xs:restriction%xs:simpleRestrictionType) -> xs:simpleType',recursive;
			'(xs:restriction|xs:restriction%xs:simpleRestrictionType) -> {(typeQName = getAttrQNameValue("base")) != null ? \n{ \n  ((el = findElementByKey ("types", typeQName)) == null) ?\n    el = CustomElement (typeQName.toXMLName (rootElement));\n\n  Enum (el)\n}}::(#CUSTOM|xs:simpleType)',recursive;
			'xs:list -> {(typeQName = getAttrQNameValue("itemType")) != null ? \n{ \n  ((el = findElementByKey ("types", typeQName)) == null) ?\n    el = CustomElement (typeQName.toXMLName (rootElement));\n\n  Enum (el)\n}}::(#CUSTOM|xs:simpleType)',recursive;
			'xs:list -> xs:simpleType',recursive;
		}
		FILTER='! instanceOf("xs:%localSimpleType") ||\ncontextElement == iterator.contextElement'
		SORTING='reversed'
		<BODY>
			<FOLDER>
				DESCR='first item'
				COND='iterator.isFirstItem && ! iterator.isLastItem'
				FMT={
					text.style='cs3';
				}
				BREAK_PARENT_BLOCK='when-executed'
				<BODY>
					<FOLDER>
						MATCHING_ET='#CUSTOM'
						FMT={
							par.option.nowrap='true';
						}
						BREAK_PARENT_BLOCK='when-executed'
						<BODY>
							<AREA_SEC>
								<AREA>
									<CTRL_GROUP>
										<CTRLS>
											<DATA_CTRL>
												ELEMENT_VALUE
											</DATA_CTRL>
										</CTRLS>
									</CTRL_GROUP>
								</AREA>
							</AREA_SEC>
						</BODY>
					</FOLDER>
					<SS_CALL>
						MATCHING_ET='xs:union'
						BREAK_PARENT_BLOCK='when-executed'
						SS_NAME='union'
					</SS_CALL>
					<AREA_SEC>
						<AREA>
							<CTRL_GROUP>
								<CTRLS>
									<SS_CALL_CTRL>
										SS_NAME='Type Name'
									</SS_CALL_CTRL>
									<PANEL>
										COND='output.format.supportsPagination && \ngetBooleanParam("fmt.page.refs") &&\nhyperTargetExists (Array (contextElement.id,  "detail"))'
										FMT={
											ctrl.size.width='171';
											text.style='cs5';
											txtfl.delimiter.type='none';
										}
										<AREA>
											<CTRL_GROUP>
												<CTRLS>
													<DELIMITER>
													</DELIMITER>
													<LABEL>
														TEXT='['
													</LABEL>
													<DATA_CTRL>
														FMT={
															ctrl.option.noHLinkFmt='true';
															text.hlink.fmt='none';
														}
														<DOC_HLINK>
															HKEYS={
																'contextElement.id';
																'"detail"';
															}
														</DOC_HLINK>
														DOCFIELD='page-htarget'
													</DATA_CTRL>
													<LABEL>
														TEXT=']'
													</LABEL>
												</CTRLS>
											</CTRL_GROUP>
										</AREA>
									</PANEL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
				</BODY>
			</FOLDER>
			<AREA_SEC>
				DESCR='middle item'
				COND='! iterator.isFirstItem && ! iterator.isLastItem'
				FMT={
					text.style='cs3';
					par.option.nowrap='true';
				}
				BREAK_PARENT_BLOCK='when-executed'
				<AREA>
					<CTRL_GROUP>
						FMT={
							txtfl.delimiter.type='none';
						}
						<CTRLS>
							<DATA_CTRL>
								FMT={
									text.option.nbsps='true';
								}
								FORMULA='dup("  ", iterator.itemNo-2) + dup("  ", iterator.itemNo-1)'
							</DATA_CTRL>
							<IMAGE_CTRL>
								IMAGE_TYPE='file-image'
								FILE='../images/inherit.gif'
							</IMAGE_CTRL>
							<SS_CALL_CTRL>
								SS_NAME='Type Name'
							</SS_CALL_CTRL>
							<PANEL>
								COND='output.format.supportsPagination && \ngetBooleanParam("fmt.page.refs") &&\nhyperTargetExists (Array (contextElement.id,  "detail"))'
								FMT={
									ctrl.size.width='171';
									text.style='cs5';
									txtfl.delimiter.type='none';
								}
								<AREA>
									<CTRL_GROUP>
										<CTRLS>
											<DELIMITER>
											</DELIMITER>
											<LABEL>
												TEXT='['
											</LABEL>
											<DATA_CTRL>
												FMT={
													ctrl.option.noHLinkFmt='true';
													text.hlink.fmt='none';
												}
												<DOC_HLINK>
													HKEYS={
														'contextElement.id';
														'"detail"';
													}
												</DOC_HLINK>
												DOCFIELD='page-htarget'
											</DATA_CTRL>
											<LABEL>
												TEXT=']'
											</LABEL>
										</CTRLS>
									</CTRL_GROUP>
								</AREA>
							</PANEL>
							<DELIMITER>
							</DELIMITER>
							<SS_CALL_CTRL>
								FMT={
									text.style='cs2';
								}
								SS_NAME='Derivation'
							</SS_CALL_CTRL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
			<AREA_SEC>
				DESCR='last item'
				COND='iterator.isLastItem && ! iterator.isFirstItem'
				FMT={
					text.style='cs3';
					par.option.nowrap='true';
				}
				<AREA>
					<CTRL_GROUP>
						FMT={
							txtfl.delimiter.type='none';
							par.margin.bottom='0';
						}
						<CTRLS>
							<DATA_CTRL>
								FMT={
									text.option.nbsps='true';
								}
								FORMULA='dup("  ", iterator.itemNo-2) + dup("  ", iterator.itemNo-1)'
							</DATA_CTRL>
							<IMAGE_CTRL>
								IMAGE_TYPE='file-image'
								FILE='../images/inherit.gif'
							</IMAGE_CTRL>
							<SS_CALL_CTRL>
								MATCHING_ETS={'xs:complexType';'xs:simpleType'}
								FMT={
									text.style='cs3';
									text.font.style.bold='true';
								}
								SS_NAME='Type Name'
							</SS_CALL_CTRL>
							<LABEL>
								MATCHING_ET='xs:%localComplexType'
								FMT={
									text.font.style.bold='true';
								}
								<DOC_HLINK>
									COND='contextElement.id != getParam("context_comp_id")'
									HKEYS={
										'contextElement.id';
										'"detail"';
									}
								</DOC_HLINK>
								TEXT='complexType'
							</LABEL>
							<LABEL>
								MATCHING_ET='xs:%localSimpleType'
								FMT={
									text.font.style.bold='true';
								}
								<DOC_HLINK>
									COND='contextElement.id != getParam("context_comp_id")'
									HKEYS={
										'contextElement.id';
										'"detail"';
									}
								</DOC_HLINK>
								TEXT='simpleType'
							</LABEL>
							<DELIMITER>
							</DELIMITER>
							<SS_CALL_CTRL>
								FMT={
									text.style='cs2';
								}
								SS_NAME='Derivation'
							</SS_CALL_CTRL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</BODY>
		<HEADER>
			<AREA_SEC>
				FMT={
					par.style='s1';
				}
				<AREA>
					<CTRL_GROUP>
						<CTRLS>
							<LABEL>
								FMT={
									text.font.size.relative='90';
								}
								TEXT='Type Derivation Tree'
							</LABEL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</HEADER>
	</ELEMENT_ITER>
</ROOT>
<STOCK_SECTIONS>
	<FOLDER>
		MATCHING_ETS={'xs:%complexType';'xs:%simpleType'}
		FMT={
			sec.outputStyle='text-par';
		}
		SS_NAME='Derivation'
		<BODY>
			<FOLDER>
				MATCHING_ET='xs:%complexType'
				BREAK_PARENT_BLOCK='when-executed'
				<BODY>
					<AREA_SEC>
						CONTEXT_ELEMENT_EXPR='findChild ("xs:simpleContent | xs:complexContent")'
						MATCHING_ETS={'xs:complexContent';'xs:simpleContent'}
						<AREA>
							<CTRL_GROUP>
								<CTRLS>
									<LABEL>
										COND='hasChild ("xs:restriction")'
										TEXT='restriction'
									</LABEL>
									<LABEL>
										COND='hasChild ("xs:extension")'
										TEXT='extension'
									</LABEL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
				</BODY>
			</FOLDER>
			<SS_CALL>
				MATCHING_ET='xs:%simpleType'
				SS_NAME='Derivation (of simpleType)'
			</SS_CALL>
		</BODY>
		<HEADER>
			<AREA_SEC>
				<AREA>
					<CTRL_GROUP>
						<CTRLS>
							<LABEL>
								TEXT='('
							</LABEL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</HEADER>
		<FOOTER>
			<AREA_SEC>
				<AREA>
					<CTRL_GROUP>
						<CTRLS>
							<LABEL>
								TEXT=')'
							</LABEL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</FOOTER>
	</FOLDER>
	<ELEMENT_ITER>
		MATCHING_ET='xs:%simpleType'
		FMT={
			sec.outputStyle='text-par';
			txtfl.delimiter.type='space';
		}
		TARGET_ETS={'xs:list';'xs:restriction'}
		SCOPE='simple-location-rules'
		RULES={
			'* -> (xs:list|xs:restriction|xs:restriction%xs:simpleRestrictionType)';
		}
		SS_NAME='Derivation (of simpleType)'
		<BODY>
			<AREA_SEC>
				MATCHING_ET='xs:restriction'
				BREAK_PARENT_BLOCK='when-executed'
				<AREA>
					<CTRL_GROUP>
						FMT={
							txtfl.delimiter.type='none';
						}
						<CTRLS>
							<LABEL>
								FMT={
									text.style='cs2';
								}
								TEXT='restriction'
							</LABEL>
							<DELIMITER>
								FMT={
									text.style='cs2';
									txtfl.delimiter.type='text';
									txtfl.delimiter.text=' of ';
								}
							</DELIMITER>
							<SS_CALL_CTRL>
								COND='getAttrValue("base") == null'
								SS_NAME='Derivation (of simpleType)'
								PASSED_ELEMENT_EXPR='findChild("xs:simpleType")'
								PASSED_ELEMENT_MATCHING_ET='xs:%localSimpleType'
							</SS_CALL_CTRL>
							<DELIMITER>
								FMT={
									txtfl.delimiter.type='none';
								}
							</DELIMITER>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
			<AREA_SEC>
				MATCHING_ET='xs:list'
				<AREA>
					<CTRL_GROUP>
						FMT={
							txtfl.delimiter.type='none';
						}
						<CTRLS>
							<LABEL>
								FMT={
									text.style='cs2';
								}
								TEXT='list'
							</LABEL>
							<DELIMITER>
								FMT={
									text.style='cs2';
									txtfl.delimiter.type='text';
									txtfl.delimiter.text=' of ';
								}
							</DELIMITER>
							<SS_CALL_CTRL>
								COND='getAttrValue("itemType") == null'
								SS_NAME='Derivation (of simpleType)'
								PASSED_ELEMENT_EXPR='findChild("xs:simpleType")'
								PASSED_ELEMENT_MATCHING_ET='xs:%localSimpleType'
							</SS_CALL_CTRL>
							<DELIMITER>
								FMT={
									txtfl.delimiter.type='none';
								}
							</DELIMITER>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</BODY>
	</ELEMENT_ITER>
	<ELEMENT_ITER>
		MATCHING_ET='xs:%simpleType'
		FMT={
			sec.outputStyle='text-par';
			txtfl.delimiter.type='space';
		}
		TARGET_ETS={'xs:list';'xs:restriction';'xs:union'}
		SCOPE='simple-location-rules'
		RULES={
			'* -> (xs:list|xs:restriction|xs:restriction%xs:simpleRestrictionType|xs:union)';
		}
		SS_NAME='simpleType (within union)'
		<BODY>
			<FOLDER>
				MATCHING_ET='xs:restriction'
				BREAK_PARENT_BLOCK='when-executed'
				<BODY>
					<AREA_SEC>
						COND='getAttrValue("base") != null'
						BREAK_PARENT_BLOCK='when-executed'
						<AREA>
							<CTRL_GROUP>
								<CTRLS>
									<LABEL>
										FMT={
											text.style='cs2';
										}
										TEXT='restriction of'
									</LABEL>
									<SS_CALL_CTRL>
										SS_NAME='Type By QName'
										PARAMS_EXPR='Array (getAttrQNameValue("base"))'
									</SS_CALL_CTRL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
					<AREA_SEC>
						<AREA>
							<CTRL_GROUP>
								FMT={
									txtfl.delimiter.type='none';
								}
								<CTRLS>
									<LABEL>
										FMT={
											text.style='cs2';
										}
										TEXT='restriction of '
									</LABEL>
									<SS_CALL_CTRL>
										SS_NAME='simpleType (within union)'
										PASSED_ELEMENT_EXPR='findChild("xs:simpleType")'
										PASSED_ELEMENT_MATCHING_ET='xs:%localSimpleType'
									</SS_CALL_CTRL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
				</BODY>
			</FOLDER>
			<FOLDER>
				MATCHING_ET='xs:list'
				BREAK_PARENT_BLOCK='when-executed'
				<BODY>
					<AREA_SEC>
						COND='getAttrValue("itemType") != null'
						BREAK_PARENT_BLOCK='when-executed'
						<AREA>
							<CTRL_GROUP>
								<CTRLS>
									<LABEL>
										FMT={
											text.style='cs2';
										}
										TEXT='list of'
									</LABEL>
									<SS_CALL_CTRL>
										SS_NAME='Type By QName'
										PARAMS_EXPR='Array (getAttrQNameValue("itemType"))'
									</SS_CALL_CTRL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
					<AREA_SEC>
						<AREA>
							<CTRL_GROUP>
								FMT={
									txtfl.delimiter.type='none';
								}
								<CTRLS>
									<LABEL>
										FMT={
											text.style='cs2';
										}
										TEXT='list of '
									</LABEL>
									<SS_CALL_CTRL>
										SS_NAME='simpleType (within union)'
										PASSED_ELEMENT_EXPR='findChild("xs:simpleType")'
										PASSED_ELEMENT_MATCHING_ET='xs:%localSimpleType'
									</SS_CALL_CTRL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
				</BODY>
			</FOLDER>
			<SS_CALL>
				MATCHING_ET='xs:union'
				SS_NAME='union'
			</SS_CALL>
		</BODY>
	</ELEMENT_ITER>
	<FOLDER>
		DESCR='param: QName of the type'
		SS_NAME='Type By QName'
		<BODY>
			<AREA_SEC>
				DESCR='if type can be resolved\n-- \nsee "Context Element" tab'
				CONTEXT_ELEMENT_EXPR='findElementByKey ("types", stockSection.param.toQName())'
				MATCHING_ETS={'xs:complexType';'xs:simpleType'}
				BREAK_PARENT_BLOCK='when-executed'
				<AREA>
					<CTRL_GROUP>
						<CTRLS>
							<DATA_CTRL>
								COND='! getAttrBooleanValue ("abstract")'
								<DOC_HLINK>
									TITLE_EXPR='instanceOf ("xs:complexType") ? "complexType" : "simpleType"'
									HKEYS={
										'contextElement.id';
										'"detail"';
									}
								</DOC_HLINK>
								FORMULA='toXMLName (stockSection.param.toQName())'
							</DATA_CTRL>
							<DATA_CTRL>
								COND='getAttrBooleanValue ("abstract")'
								FMT={
									text.font.style.italic='true';
								}
								<DOC_HLINK>
									TITLE_EXPR='instanceOf ("xs:complexType") ?\n  "abstract complexType" : "abstract simpleType"'
									HKEYS={
										'contextElement.id';
										'"detail"';
									}
								</DOC_HLINK>
								FORMULA='toXMLName (stockSection.param.toQName())'
							</DATA_CTRL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
			<AREA_SEC>
				DESCR='otherwise, the type cannot be resolved'
				<AREA>
					<CTRL_GROUP>
						<CTRLS>
							<DATA_CTRL>
								FORMULA='toXMLName (stockSection.param.toQName())'
							</DATA_CTRL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</BODY>
	</FOLDER>
	<AREA_SEC>
		DESCR='prints the XML (qualified or local) name of a global type component (passed in the stock-section context element)'
		SS_NAME='Type Name'
		<AREA>
			<CTRL_GROUP>
				FMT={
					txtfl.delimiter.type='none';
				}
				<CTRLS>
					<DATA_CTRL>
						COND='! getAttrBooleanValue ("abstract")'
						<DOC_HLINK>
							COND='contextElement.id != getParam("context_comp_id")'
							TITLE_EXPR='instanceOf ("xs:complexType") ? "complexType" : "simpleType"'
							HKEYS={
								'contextElement.id';
								'"detail"';
							}
						</DOC_HLINK>
						FORMULA='toXMLName (\n  findAncestor ("xs:schema").getAttrStringValue("targetNamespace"), \n  getAttrStringValue("name"),\n  Enum (rootElement, contextElement)\n)'
					</DATA_CTRL>
					<DATA_CTRL>
						COND='getAttrBooleanValue ("abstract")'
						FMT={
							text.font.style.italic='true';
						}
						<DOC_HLINK>
							COND='contextElement.id != getParam("context_comp_id")'
							TITLE_EXPR='instanceOf ("xs:complexType") ? \n  "abstract complexType" : "abstract simpleType"'
							HKEYS={
								'contextElement.id';
								'"detail"';
							}
						</DOC_HLINK>
						FORMULA='toXMLName (\n  findAncestor ("xs:schema").getAttrStringValue("targetNamespace"), \n  getAttrStringValue("name"),\n  Enum (rootElement, contextElement)\n)'
					</DATA_CTRL>
				</CTRLS>
			</CTRL_GROUP>
		</AREA>
	</AREA_SEC>
	<FOLDER>
		MATCHING_ET='xs:union'
		FMT={
			sec.outputStyle='text-par';
			txtfl.delimiter.type='text';
			txtfl.delimiter.text=' | ';
		}
		SS_NAME='union'
		<BODY>
			<ATTR_ITER>
				SCOPE='attr-values'
				ATTR='memberTypes'
				<BODY>
					<AREA_SEC>
						<AREA>
							<CTRL_GROUP>
								<CTRLS>
									<SS_CALL_CTRL>
										SS_NAME='Type By QName'
										PARAMS_EXPR='Array (iterator.value.toQName())'
									</SS_CALL_CTRL>
								</CTRLS>
							</CTRL_GROUP>
						</AREA>
					</AREA_SEC>
				</BODY>
			</ATTR_ITER>
			<ELEMENT_ITER>
				TARGET_ET='xs:%localSimpleType'
				SCOPE='simple-location-rules'
				RULES={
					'* -> xs:simpleType%xs:localSimpleType';
				}
				<BODY>
					<SS_CALL>
						SS_NAME='simpleType (within union)'
					</SS_CALL>
				</BODY>
			</ELEMENT_ITER>
		</BODY>
		<HEADER>
			<AREA_SEC>
				<AREA>
					<CTRL_GROUP>
						FMT={
							txtfl.delimiter.type='space';
						}
						<CTRLS>
							<LABEL>
								FMT={
									text.style='cs2';
								}
								TEXT='union of'
							</LABEL>
							<LABEL>
								TEXT='('
							</LABEL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</HEADER>
		<FOOTER>
			<AREA_SEC>
				<AREA>
					<CTRL_GROUP>
						<CTRLS>
							<LABEL>
								TEXT=')'
							</LABEL>
						</CTRLS>
					</CTRL_GROUP>
				</AREA>
			</AREA_SEC>
		</FOOTER>
	</FOLDER>
</STOCK_SECTIONS>
CHECKSUM='kkxAXDGMttfvSaR03r6QKiar55+NFyrKZgUBnxEk+Eg'
</DOCFLEX_TEMPLATE>