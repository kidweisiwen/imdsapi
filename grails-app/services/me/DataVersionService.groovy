package me

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Transactional
class DataVersionService {
    def dataSource

    @Transactional
    def uploadExcelData(file) {
        def data = []
        def originalFilename = file.getOriginalFilename()
        def suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        InputStream input = new ByteArrayInputStream(file.bytes);
        Workbook workbook = WorkbookFactory.create(input);
        def sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows(); //获取总行数
        int sheetCount = workbook.getNumberOfSheets();  //Sheet的数量
        (0..rowCount).eachWithIndex { int entry, int i ->
            data.add([a:1])
        }
        println rowCount
        return data
    }

    @Transactional
    def createVersionTable(version) {
        def ver = version + ""
        def sql = new Sql(dataSource)
        ver = ver.replace(".", "_")
        def rows = sql.rows("select count(*) from pg_class where relname = 'my_data_version_${ver}'".toString())
        if (rows[0].count == 0) {
            def sqlString = """
          CREATE TABLE public.my_data_version_${ver}
          (
              id varchar(32),
              project_id bigint,
              content jsonb,
              level_col jsonb,
              oempartno_col jsonb,
              jcpartno_col jsonb,
              partdesc_col jsonb,
              info_col jsonb,
              extension_col jsonb,
              order_flag bigint
          );
          CREATE UNIQUE INDEX idx_uniq_my_data_version_${ver}_id ON public.my_data_version_${ver}(id);
          CREATE INDEX idx_btree_my_data_version_${ver}_order_flag ON public.my_data_version_${ver} using btree (order_flag);
          CREATE INDEX idx_gin_my_data_version_${ver}_content ON public.my_data_version_${ver} USING gin (content);
          CREATE INDEX idx_btree_my_data_version_${ver}_project_id on public.my_data_version_${ver} using btree (project_id);
            """.toString()
            //println sqlString
            sql.executeUpdate(sqlString)
        }

    }

    @Transactional(readOnly = true)
    def queryTable(ver,subProjectId) {
        def sql = new Sql(dataSource)
        def tableName = "data_version_"+ver.replace(".", "_")
        def querySql = """
            select a.id as id,a.flag as flag,sub_project_id as subProjectId,
            (content #>> '{level}')::varchar as level,
            (content #>> '{levels}')::varchar as levels,
            (content #>> '{_PARENT}')::varchar as parent,
            (content #>> '{info}')::varchar as info,
            (content #>> '{partDesc}')::varchar as partDesc,
            (content #>> '{jcPartNo}')::varchar as jcPartNo,
            (content #>> '{oemPartNo}')::varchar as oemPartNo,
            
            (content #>> '{wig}')::varchar as wig,
            (content #>> '{st}')::varchar as st,
            (content #>> '{cm}')::varchar as cm,
            (content #>> '{supplier}')::varchar as supplier,
            (content #>> '{sici}')::varchar as sici,
            (content #>> '{sds}')::varchar as sds,
            (content #>> '{jici}')::varchar as jici,
            (content #>> '{ppmcNo}')::varchar as ppmcNo,
            (content #>> '{colourName}')::varchar as colourName,
            (content #>> '{scjp}')::varchar as scjp,
            (content #>> '{pden}')::varchar as pden,
            (content #>> '{cd}')::varchar as cd,
            (content #>> '{ccam}')::varchar as ccam,
            (content #>> '{ccab}')::varchar as ccab,
            (content #>> '{dsiic}')::varchar as dsiic,
            (content #>> '{_PARENT_JPN}')::varchar as parentJpn,
            (content #>> '{_PARENT_LEVEL}')::varchar as parentLevel
            from ${tableName} a 
            where sub_project_id=?
            order by a.flag asc
        """.toString()
        //println querySql
        def rows = sql.rows(querySql,subProjectId)
        return rows
    }
}
